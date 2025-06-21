package com.david.follow_service.service;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.PageResponse;
import com.david.common.dto.follow.FollowCreationMessage;
import com.david.common.dto.follow.FollowResponse;
import com.david.follow_service.entity.UserFollow;
import com.david.follow_service.exception.AlreadyFollowingException;
import com.david.follow_service.exception.NotFollowingException;
import com.david.follow_service.exception.SelfFollowException;
import com.david.follow_service.mapper.FollowMapper;
import com.david.follow_service.mapper.ProfileClient;
import com.david.follow_service.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FollowService {

    private final FollowRepository followRepository;
    private final FollowMapper followMapper;
    private final ProfileClient profileClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.routing-key.followed}")
    private String followedRoutingKey;
    @Value("${app.rabbitmq.routing-key.unfollowed}")
    private String unfollowedRoutingKey;

    public void followUser(String followedId) {
        log.info("FollowService::followUser - Attempting to follow user with ID: {}", followedId);
        var jwt = getJwt();
        String followerId = jwt.getSubject();

        if (followerId.equals(followedId)) {
            throw new SelfFollowException();
        }

        var response = profileClient.userExistsById(followedId);
        if (response.getStatus() != 200) {
            log.error("User with ID {} does not exist", followedId);
            throw new IllegalArgumentException("User does not exist");
        }

        if (followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            throw new AlreadyFollowingException(followerId, followedId);
        }

        var follow = followMapper.toEntity(followerId, followedId);
        followRepository.save(follow);
        log.info("FollowService::followUser - User with ID {} successfully followed user with ID {}", followerId, followedId);

        try {
            rabbitTemplate.convertAndSend(followedRoutingKey, ApiEventMessage.builder()
                            .eventId(UUID.randomUUID().toString())
                            .eventType("FOLLOW")
                            .timestamp(String.valueOf(System.currentTimeMillis()))
                            .payload(FollowCreationMessage.builder()
                                    .id(follow.getId())
                                    .followerId(followerId)
                                    .followedId(followedId)
                                    .createdAt(follow.getCreatedAt())
                                    .build())
                            .build()
            );
            log.info("FollowService::followUser - Follow event sent to RabbitMQ for followerId: {}, followedId: {}", followerId, followedId);
        } catch (Exception e) {
            log.error("Failed to send follow event to RabbitMQ: {}", e.getMessage());
            throw new RuntimeException("Failed to send follow event", e);
        }
    }

    public void unFollowUser(String followedId) {
        log.info("FollowService::unFollowUser - Attempting to unfollow user with ID: {}", followedId);
        var jwt = getJwt();
        String followerId = jwt.getSubject();

        if (followerId.equals(followedId)) {
            throw new SelfFollowException();
        }

        var response = profileClient.userExistsById(followedId);
        if (response.getStatus() != 200) {
            log.error("User with ID {} does not exist", followedId);
            throw new IllegalArgumentException("User does not exist");
        }

        if (!followRepository.existsByFollowerIdAndFollowedId(followerId, followedId)) {
            log.warn("User with ID {} is not following user with ID {}", followerId, followedId);
            throw new NotFollowingException(followerId, followedId);
        }

        followRepository.deleteByFollowerIdAndFollowedId(followerId, followedId);
        log.info("FollowService::unFollowUser - User with ID {} successfully unfollowed user with ID {}", followerId, followedId);
//        try {
//            rabbitTemplate.convertAndSend(unfollowedRoutingKey, ApiEventMessage.builder()
//                            .eventId(UUID.randomUUID().toString())
//                            .eventType("UNFOLLOW")
//                            .timestamp(String.valueOf(System.currentTimeMillis()))
//                            .payload(followMapper.toEntity(followerId, followedId))
//                            .build()
//            );
//            log.info("FollowService::unFollowUser - Unfollow event sent to RabbitMQ for followerId: {}, followedId: {}", followerId, followedId);
//        } catch (Exception e) {
//            log.error("Failed to send unfollow event to RabbitMQ: {}", e.getMessage());
//            throw new RuntimeException("Failed to send unfollow event", e);
//        }
    }

    public boolean isFollowing(String followedId) {
        log.info("FollowService::isFollowing - Checking if user with ID {} is following user with ID {}", getJwt().getSubject(), followedId);
        var jwt = getJwt();
        String followerId = jwt.getSubject();

        if (followerId.equals(followedId)) {
            throw new SelfFollowException();
        }

        var response = profileClient.userExistsById(followedId);
        if (response.getStatus() != 200) {
            log.error("User with ID {} does not exist", followedId);
            throw new IllegalArgumentException("User does not exist");
        }

        boolean isFollowing = followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
        log.info("FollowService::isFollowing - User with ID {} is {}following user with ID {}", followerId, isFollowing ? "" : "not ", followedId);
        return isFollowing;
    }

    public PageResponse<?> getFollowers(String userId, int page, int size, String sortBy) {
        int pageNumber = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(pageNumber, size, sortOrder);
        Page<UserFollow> followsPage = followRepository.findByFollowedId(userId, pageable);
        List<FollowResponse> followResponses = followsPage.stream()
                .map(followMapper::toDto)
                .toList();
        log.info("FollowService::getFollowers - Retrieved {} follower records for user ID {}", followResponses.size(), userId);
        return PageResponse.builder()
                .page(page)
                .size(size)
                .totalPages(followsPage.getTotalPages())
                .totalElements(followsPage.getTotalElements())
                .contents(followResponses)
                .build();
    }

    public PageResponse<?> getFollowing(String userId, int page, int size, String sortBy) {
        int pageNumber = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(pageNumber, size, sortOrder);
        Page<UserFollow> followsPage = followRepository.findByFollowerId(userId, pageable);
        List<FollowResponse> followResponses = followsPage.stream()
                .map(followMapper::toDto)
                .toList();
        log.info("FollowService::getFollowing - Retrieved {} following records for user ID {}", followResponses.size(), userId);
        return PageResponse.builder()
                .page(page)
                .size(size)
                .totalPages(followsPage.getTotalPages())
                .totalElements(followsPage.getTotalElements())
                .contents(followResponses)
                .build();
    }

    private Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::createTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        return jwt;
    }
}
