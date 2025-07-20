package com.david.tweet_service.service;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.media.MediaResponse;
import com.david.common.dto.tweet.StatsResponse;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.common.dto.tweet.TweetLikedEventPayload;
import com.david.common.dto.tweet.TweetResponse;
import com.david.common.enums.Visibility;
import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.entity.Tweet;
import com.david.tweet_service.exception.TweetNotFoundException;
import com.david.tweet_service.exception.TweetServiceException;
import com.david.tweet_service.mapper.MediaMapper;
import com.david.tweet_service.mapper.TweetMapper;
import com.david.tweet_service.repository.MediaClient;
import com.david.tweet_service.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TweetService {

    private final TweetRepository tweetRepository;
    private final MediaClient mediaClient;
    private final RabbitTemplate rabbitTemplate;
    private final TweetMapper tweetMapper;
    private final MediaMapper mediaMapper;

    @Value("${app.rabbitmq.routing-key.tweet-created}")
    private String tweetCreatedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-liked}")
    private String tweetLikedRoutingKey;
    @Value("${app.rabbitmq.routing-key.tweet-deleted}")
    private String tweetDeletedRoutingKey;


    //    @Cacheable(value = "cacheTweet", key = "#tweetId")
    public TweetResponse getTweetById(String tweetId) {
        log.info("TweetService::getTweetById - Execution started for tweetId: {}", tweetId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Tweet savedTweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Tweet not found with id: " + tweetId));

        if (savedTweet.getVisibility() == Visibility.PRIVATE) {
            if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getSubject();
                if (!savedTweet.getUserId().equals(userId)) {
                    log.warn("TweetService::getTweetById - User {} is not authorized to view tweet {}", userId, tweetId);
                    throw new TweetNotFoundException("Tweet not found with id: " + tweetId);
                }
            } else {
                log.warn("TweetService::getTweetById - Unauthorized access attempt for tweetId: {}", tweetId);
                throw new TweetNotFoundException("Tweet not found with id: " + tweetId);
            }
        }

        log.info("TweetService::getTweetById - Execution ended for tweetId: {}", tweetId);
        return tweetMapper.toDto(savedTweet);
    }

    public List<TweetResponse> getTweetsByIds(List<String> tweetIds) {
        log.info("TweetService::getTweetsByIds - Execution started for tweetIds: {}", tweetIds);
        List<TweetResponse> tweetResponses = tweetRepository.findAllById(tweetIds).stream()
                .map(tweetMapper::toDto)
                .toList();
        log.info("TweetService::getTweetsByIds - Execution ended for tweetIds: {}", tweetIds);
        return tweetResponses;
    }

    public List<TweetResponse> getMyTweets(int page, int size, String sortBy) {
        log.info("TweetService::getMyTweets - Execution started");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::getMyTweets - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        System.out.println(userId);
        long totalTweets = tweetRepository.countByUserId(userId);
        log.info("Total tweets for user {}: {}", userId, totalTweets);

        int p = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort.Direction direction = sortParams.length > 1 ? Sort.Direction.fromString(sortParams[1]) : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(direction, sortParams[0]);
        Pageable pageable = PageRequest.of(p, size, sortOrder);
        List<TweetResponse> tweetResponses = tweetRepository.findAllByUserId(userId, pageable).stream()
                .map(tweetMapper::toDto)
                .toList();
        log.info("TweetService::getMyTweets - Execution ended");
        return tweetResponses;
    }

    public List<TweetResponse> getPublicTweets(String userId, int page, int size, String sortBy) {
        log.info("TweetService::getPublicTweets - Execution started for userId: {}", userId);
        int p = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort.Direction direction = sortParams.length > 1 ? Sort.Direction.fromString(sortParams[1]) : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(direction, sortParams[0]);
        Pageable pageable = PageRequest.of(p, size, sortOrder);
        List<TweetResponse> tweetResponses = tweetRepository.findAllByUserIdAndVisibility(userId, Visibility.PUBLIC, pageable).stream()
                .map(tweetMapper::toDto)
                .toList();
        log.info("TweetService::getPublicTweets - Execution ended");
        return tweetResponses;
    }

    @Transactional
//    @CachePut(value = "cacheTweet", key = "#result.id")
    public TweetResponse createTweet(TweetRequest tweetRequest, MultipartFile[] mediaFiles) {
        log.info("TweetService::createTweet - Execution started");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::createTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        Tweet tweet = tweetMapper.toEntity(tweetRequest);
        List<MediaResponse> mediaList = new ArrayList<>();
        if (mediaFiles != null && mediaFiles.length > 0) {
            log.info("TweetService::createTweet - Processing media files for tweet");
            try {
                FeignApiResponse<List<MediaResponse>> mediaResponse = mediaClient.uploadFiles(mediaFiles, "MEDIA");
                if (mediaResponse != null) {
                    mediaList = mediaResponse.getResult();
                    log.info("ProfileService::createTweet - Media files uploaded successfully");
                }
                tweet.setMediaItems(mediaList.stream()
                        .map(mediaMapper::toEntity)
                        .toList());
            } catch (Exception e) {
                log.error("TweetService::createTweet - Exception occurred while uploading media files: {}", e.getMessage());
                throw new TweetServiceException("Failed to upload media files" + e.getMessage());
            }
        }
        tweet.setUserId(userId);
        Tweet savedTweet = tweetRepository.save(tweet);
        log.info("TweetService::createTweet - Tweet saved with id: {}", savedTweet.getId());
        TweetCreatedEventPayload tweetCreatedEventPayload = TweetCreatedEventPayload.builder()
                .id(savedTweet.getId())
                .userId(savedTweet.getUserId())
                .content(savedTweet.getContent())
                .mediaItems(savedTweet.getMediaItems().stream()
                        .map(mediaMapper::toDto)
                        .toList())
                .hashtags(savedTweet.getHashtags())
                .statsResponse(StatsResponse.builder()
                        .likesCount(savedTweet.getStats().getLikesCount())
                        .build())
                .visibility(savedTweet.getVisibility())
                .likedBy(savedTweet.getLikedBy())
                .createdAt(savedTweet.getCreatedAt())
                .updatedAt(savedTweet.getUpdatedAt())
                .build();

        try {
            rabbitTemplate.convertAndSend(tweetCreatedRoutingKey, ApiEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TWEET_CREATED")
                    .timestamp(String.valueOf(System.currentTimeMillis()))
                    .payload(tweetCreatedEventPayload)
                    .build());
            log.info("TweetService::getTweetById - TweetCreatedEvent published for tweetId: {}", tweetCreatedEventPayload.getId());
        } catch (Exception e) {
            log.error("TweetService::getTweetById - Failed to publish TweetCreatedEvent for tweetId: {}. Error: {}", tweetCreatedEventPayload.getId(), e.getMessage());
            throw new TweetServiceException("Failed to publish tweet created event: " + e.getMessage());
        }
        log.info("TweetService::createTweet - Execution ended");
        return tweetMapper.toDto(savedTweet);
    }

    @Transactional
    public TweetResponse likeTweet(String tweetId) {
        log.info("TweetService::likeTweet - Execution started");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::likeTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Not found tweet with tweetId: " + tweetId));

        if (tweet.getLikedBy().contains(userId)) {
            log.info("User {} already liked tweet {}", userId, tweetId);
        } else {
            tweet.getLikedBy().add(userId);
            tweet.getStats().setLikesCount(tweet.getStats().getLikesCount() + 1);
            log.info("User {} liked tweet {}", userId, tweetId);

            try {
                rabbitTemplate.convertAndSend(tweetLikedRoutingKey, ApiEventMessage.builder()
                        .eventId(UUID.randomUUID().toString())
                        .eventType("TWEET_LIKED")
                        .timestamp(String.valueOf(System.currentTimeMillis()))
                        .payload(TweetLikedEventPayload.builder()
                                .tweetId(tweetId)
                                .userId(userId)
                                .createdAt(String.valueOf(System.currentTimeMillis()))
                                .build())
                        .build());
                log.info("Published TweetLikedEvent for {}", tweetId);
            } catch (Exception e) {
                log.error("Failed to publish liked event: {}", e.getMessage());
                throw new TweetServiceException("Cannot publish liked event", e);
            }
        }
        log.info("TweetService::likeTweet - Execution ended");
        return tweetMapper.toDto(tweetRepository.save(tweet));
    }

    @Transactional
//    @CachePut(value = "cacheTweet", key = "#tweetId")
    public TweetResponse unlikeTweet(String tweetId) {
        log.info("TweetService::unlikeTweet - Execution started");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::unlikeTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Not found tweet with tweetId: " + tweetId));

        if (tweet.getLikedBy().remove(userId)) {
            tweet.getStats().setLikesCount(Math.max(0, tweet.getStats().getLikesCount() - 1));
            log.info("User {} unliked tweet {}", userId, tweetId);
        } else {
            log.info("User {} had not liked tweet {}", userId, tweetId);
        }
        log.info("TweetService::unlikeTweet - Execution ended");
        return tweetMapper.toDto(tweetRepository.save(tweet));
    }

    @Transactional
    //    @CacheEvict(value = "cacheTweet", key = "#tweetId")
    public void deleteTweet(String tweetId) {
        log.info("TweetService::deleteTweet - Execution started for tweetId: {}", tweetId);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof Jwt jwt)) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Tweet not found with id: " + tweetId));
        if (!tweet.getUserId().equals(jwt.getSubject())) {
            throw new AccessDeniedException("You are not the owner of this tweet");
        }
        tweetRepository.delete(tweet);
        log.info("TweetService::deleteTweet - Tweet deleted with id: {}", tweetId);
        try {
            rabbitTemplate.convertAndSend(tweetDeletedRoutingKey, ApiEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("TWEET_DELETED")
                    .timestamp(String.valueOf(System.currentTimeMillis()))
                    .payload(tweetId)
                    .build());
            log.info("TweetService::deleteTweet - TweetDeletedEvent published for tweetId: {}", tweetId);
        } catch (Exception e) {
            log.error("TweetService::deleteTweet - Failed to publish TweetDeletedEvent for tweetId: {}. Error: {}", tweetId, e.getMessage());
            throw new TweetServiceException("Failed to publish tweet deleted event: " + e.getMessage());
        }
        log.info("TweetService::deleteTweet - Execution ended");
    }
}
