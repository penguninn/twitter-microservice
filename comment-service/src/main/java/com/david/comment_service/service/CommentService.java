package com.david.comment_service.service;

import com.david.comment_service.dto.event.CommentCreateEvent;
import com.david.comment_service.dto.request.CommentRequest;
import com.david.comment_service.entity.Comment;
import com.david.comment_service.exception.CommentNotFoundException;
import com.david.comment_service.exception.CommentServiceException;
import com.david.comment_service.mapper.CommentMapper;
import com.david.comment_service.repository.CommentRepository;
import com.david.comment_service.repository.TweetClient;
import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.comment.CommentResponse;
import com.david.common.dto.tweet.TweetResponse;
import com.david.common.enums.CommentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CommentMapper commentMapper;
    private final TweetClient tweetClient;

    @Value("${app.rabbitmq.exchange.comment-events}")
    private String commentEventExchange;
    @Value("${app.rabbitmq.routing-key.comment-created}")
    private String commentCreatedRoutingKey;
    @Value("${app.rabbitmq.routing-key.comment-deleted}")
    private String commentDeletedRoutingKey;

    public PageResponse<?> getTopLevelCommentsByTweetId(String tweetId, int page, int size, String sortBy) {
        log.info("CommentService::getCommentsByTweetId - Execution started for tweetId: {}", tweetId);
        int pageNumber = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(pageNumber, size, sortOrder);
        Page<Comment> comments = commentRepository.findAllByTweetIdAndType(tweetId, CommentType.PARENT, pageable);
        List<CommentResponse> commentResponses = comments.stream()
                .map(commentMapper::toDto)
                .toList();
        log.info("CommentService::getCommentsByTweetId - Execution ended for tweetId: {}", tweetId);
        return PageResponse.builder()
                .contents(commentResponses)
                .page(comments.getNumber() + 1)
                .size(comments.getSize())
                .totalElements(comments.getTotalElements())
                .totalPages(comments.getTotalPages())
                .build();
    }

    public PageResponse<?> getRepliesByCommentId(String commentId, int page, int size, String sortBy) {
        log.info("CommentService::getRepliesByCommentId - Execution started for commentId: {}", commentId);
        int pageNumber = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(pageNumber, size, sortOrder);
        Page<Comment> replies = commentRepository.findAllByParentIdAndType(commentId, CommentType.REPLY, pageable);
        List<CommentResponse> replyResponses = replies.stream()
                .map(commentMapper::toDto)
                .toList();
        log.info("CommentService::getRepliesByCommentId - Execution ended for commentId: {}", commentId);
        return PageResponse.builder()
                .contents(replyResponses)
                .page(replies.getNumber() + 1)
                .size(replies.getSize())
                .totalElements(replies.getTotalElements())
                .totalPages(replies.getTotalPages())
                .build();
    }

    @Transactional
    public CommentResponse createComment(String tweetId, String parentId, CommentRequest request) {
        log.info("CommentService::createComment - Execution started");
        Jwt jwt = getJwt();

        String actualTweetId;
        CommentType type;
        if (parentId != null && !parentId.isBlank()) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException("Parent comment not found: " + parentId));
            actualTweetId = parent.getTweetId();
            type = CommentType.REPLY;
        } else if (tweetId != null && !tweetId.isBlank()) {
            isTweetExists(tweetId);
            actualTweetId = tweetId;
            type = CommentType.PARENT;
        } else {
            throw new CommentServiceException("Either tweetId or parentId must be provided");
        }

        Comment commentCreation = commentMapper.toEntity(request);
        commentCreation.setType(type);
        commentCreation.setParentId(parentId);
        commentCreation.setTweetId(actualTweetId);
        commentCreation.setUserId(jwt.getSubject());

        Comment savedComment = commentRepository.save(commentCreation);

        log.info("TweetService::createComment - Comment saved");
        CommentCreateEvent commentCreateEvent = CommentCreateEvent.builder()
                .commentId(savedComment.getId())
                .tweetId(tweetId)
                .userId(jwt.getSubject())
                .content(savedComment.getContent())
                .parentId(parentId)
                .createdAt(savedComment.getCreatedAt())
                .build();
        try {
            rabbitTemplate.convertAndSend(commentEventExchange, commentCreatedRoutingKey, commentCreateEvent);
            log.info("CommentService::createComment - Comment creation event sent to RabbitMQ");
        } catch (Exception e) {
            log.error("CommentService::createComment - Failed to send comment creation event", e);
        }
        log.info("CommentService::createComment - Execution ended");
        return commentMapper.toDto(savedComment);
    }


    @Transactional
    public CommentResponse updateComment(String commentId, CommentRequest request) {
        log.info("CommentService::updateComment - Execution started for commentId: {}", commentId);
        Jwt jwt = getJwt();
        Comment existingComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        String userId = jwt.getSubject();
        if (!existingComment.getUserId().equals(userId)) {
            log.warn("CommentService::updateComment - Unauthorized update attempt by userId: {}", userId);
            throw new AccessDeniedException("Unauthorized update attempt");
        }
        isTweetExists(existingComment.getTweetId());
        existingComment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(existingComment);

        log.info("CommentService::updateComment - Comment updated successfully");
        try {
            rabbitTemplate.convertAndSend(commentEventExchange, commentCreatedRoutingKey, Map.of("commentId", updatedComment.getId()));
            log.info("CommentService::updateComment - Comment update event sent to RabbitMQ");
        } catch (Exception e) {
            log.error("CommentService::updateComment - Failed to send comment update event", e);
        }
        log.info("CommentService::updateComment - Execution ended for commentId: {}", commentId);
        return commentMapper.toDto(updatedComment);
    }

    @Transactional
    public void deleteComment(String commentId) {
        log.info("CommentService::deleteComment - Execution started for commentId: {}", commentId);
        Jwt jwt = getJwt();
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found"));
        String userId = jwt.getSubject();
        boolean isOwner = comment.getUserId().equals(userId);
        boolean isParentOwner = comment.getType() == CommentType.REPLY &&
                commentRepository.findById(comment.getParentId())
                        .map(parent -> userId.equals(parent.getUserId()))
                        .orElse(false);
        FeignApiResponse<TweetResponse> tweetResponseData = tweetClient.getTweetById(comment.getTweetId());
        if (tweetResponseData.getStatus() != 200) {
            log.error("CommentService::deleteComment - Tweet with ID {} not found", comment.getTweetId());
            throw new CommentServiceException("Tweet not found");
        }
        boolean isTweetOwner = tweetResponseData.getResult().getUserId().equals(userId);
        if (!isOwner && !isParentOwner && !isTweetOwner) {
            log.warn("CommentService::deleteComment - Unauthorized deletion attempt by userId: {}", userId);
            throw new AccessDeniedException("Forbidden");
        }
        commentRepository.deleteAllByParentId(comment.getId());
        commentRepository.delete(comment);
        log.info("CommentService::deleteComment - Comment deleted successfully");
        try {
            rabbitTemplate.convertAndSend(commentEventExchange, commentDeletedRoutingKey, Map.of("commentId", commentId));
            log.info("CommentService::deleteComment - Comment deletion event sent to RabbitMQ");
        } catch (Exception e) {
            log.error("CommentService::deleteComment - Failed to send comment deletion event", e);
        }
    }

    private Jwt getJwt() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::createTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        return jwt;
    }

    private void isTweetExists(String tweetId) {
        if (tweetClient.getTweetById(tweetId).getStatus() != 200) {
            throw new CommentServiceException("Tweet not found: " + tweetId);
        }
    }

}
