package com.david.comment_service.service;

import com.david.comment_service.dto.request.CommentRequest;
import com.david.comment_service.entity.Comment;
import com.david.comment_service.exception.CommentNotFoundException;
import com.david.comment_service.exception.CommentServiceException;
import com.david.comment_service.mapper.CommentMapper;
import com.david.comment_service.repository.CommentRepository;
import com.david.comment_service.repository.TweetClient;
import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.comment.CommentCreatedEventPayload;
import com.david.common.dto.comment.CommentResponse;
import com.david.common.dto.tweet.TweetResponse;
import com.david.common.enums.CommentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentService {

    private final CommentRepository commentRepository;
    private final RabbitTemplate rabbitTemplate;
    private final CommentMapper commentMapper;
    private final TweetClient tweetClient;
    private final MongoTemplate mongoTemplate;

    @Value("${app.rabbitmq.routing-key.comment-created}")
    private String commentCreatedRoutingKey;

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
        if (StringUtils.hasText(parentId)) {
            Comment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new CommentNotFoundException("Parent comment not found: " + parentId));
            actualTweetId = parent.getTweetId();
            type = CommentType.REPLY;
        } else if (StringUtils.hasText(tweetId)) {
            isTweetExists(tweetId);
            actualTweetId = tweetId;
            type = CommentType.PARENT;
        } else {
            throw new CommentServiceException("Either tweetId or parentId must be provided");
        }

        Comment commentCreation = commentMapper.toEntity(request);
        commentCreation.setType(type);
        commentCreation.setParentId(StringUtils.hasText(parentId) ? parentId : null);
        commentCreation.setTweetId(actualTweetId);
        commentCreation.setUserId(jwt.getSubject());

        Comment savedComment = commentRepository.save(commentCreation);

        log.info("TweetService::createComment - Comment saved");
        CommentCreatedEventPayload commentCreateEvent = CommentCreatedEventPayload.builder()
                .commentId(savedComment.getId())
                .tweetId(tweetId)
                .userId(jwt.getSubject())
                .content(savedComment.getContent())
                .parentId(parentId)
                .createdAt(savedComment.getCreatedAt())
                .build();
        try {
            rabbitTemplate.convertAndSend(commentCreatedRoutingKey, ApiEventMessage.builder()
                    .eventId(UUID.randomUUID().toString())
                    .eventType("COMMENT_CREATED")
                    .timestamp(String.valueOf(savedComment.getCreatedAt()))
                    .payload(commentCreateEvent)
                    .build());
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
        isTweetExists(existingComment.getTweetId().toString());
        existingComment.setContent(request.getContent());
        Comment updatedComment = commentRepository.save(existingComment);
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
                commentRepository.findById(comment.getParentId().toString())
                        .map(parent -> userId.equals(parent.getUserId()))
                        .orElse(false);
        FeignApiResponse<TweetResponse> tweetResponseData = tweetClient.getTweetById(comment.getTweetId().toString());
        if (tweetResponseData.getStatus() != 200) {
            throw new CommentServiceException("Tweet not found");
        }
        boolean isTweetOwner = tweetResponseData.getResult().getUserId().equals(userId);

        if (!isOwner && !isParentOwner && !isTweetOwner) {
            throw new AccessDeniedException("Forbidden");
        }

        List<Comment> descendants = getAllDescendants(commentId);

        List<String> idsToDelete = new ArrayList<>(descendants.stream().map(Comment::getId).toList());
        idsToDelete.add(commentId);

        commentRepository.deleteAllById(idsToDelete);
        log.info("CommentService::deleteComment - Deleted {} comment(s)", idsToDelete.size());
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

    public List<Comment> getAllDescendants(String rootCommentId) {
        ObjectId rootObjectId = new ObjectId(rootCommentId);

        MatchOperation matchRoot = Aggregation.match(Criteria.where("_id").is(rootObjectId));

        AggregationOperation addIdStrField = context -> new Document("$addFields",
                new Document("idStr", new Document("$toString", "$_id")));

        GraphLookupOperation graphLookup = GraphLookupOperation.builder()
                .from("comments")
                .startWith("$idStr")
                .connectFrom("idStr")
                .connectTo("parentId")
                .as("descendants");

        Aggregation aggregation = Aggregation.newAggregation(
                matchRoot,
                addIdStrField,
                graphLookup
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "comments", Document.class);
        Document rootDoc = results.getUniqueMappedResult();

        if (rootDoc == null || !rootDoc.containsKey("descendants")) {
            return List.of();
        }

        Object descendantsObj = rootDoc.get("descendants");
        if (!(descendantsObj instanceof List<?> list)) {
            log.warn("CommentService::getAllDescendants - Type error");
            return List.of();
        }

        List<Document> descendantsDocs = list.stream()
                .filter(item -> item instanceof Document)
                .map(item -> (Document) item)
                .toList();

        return descendantsDocs.stream()
                .map(doc -> mongoTemplate.getConverter().read(Comment.class, doc))
                .toList();
    }

}
