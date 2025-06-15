package com.david.comment_service.controller;

import com.david.comment_service.dto.request.CommentRequest;
import com.david.comment_service.service.CommentService;
import com.david.common.dto.ApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.comment.CommentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/")
public class CommentController {

    private  final CommentService commentService;

    @GetMapping("/tweets/{tweetId}/comments")
    public ApiResponse<?> getCommentsByTweetId(
            @PathVariable("tweetId") String tweetId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("CommentService::getCommentsByTweetId - Execution started for tweetId: {}", tweetId);
        PageResponse<?> response = commentService.getTopLevelCommentsByTweetId(tweetId, page, size, sortBy);
        log.info("CommentService::getCommentsByTweetId - Execution started for ended: {}", tweetId);
        return new ApiResponse<>(HttpStatus.OK, "Comments fetched successfully", response);
    }

    @GetMapping("/comments/{commentId}/replies")
    public ApiResponse<?> getRepliesByCommentId(
            @PathVariable("commentId") String commentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt,desc") String sortBy
    ) {
        log.info("CommentService::getRepliesByCommentId - Execution started for commentId: {}", commentId);
        PageResponse<?> response = commentService.getRepliesByCommentId(commentId, page, size, sortBy);
        log.info("CommentService::getRepliesByCommentId - Execution started for ended");
        return new ApiResponse<>(HttpStatus.OK, "Comments fetched successfully", response);
    }

    @PostMapping("/tweets/{tweetId}/comments")
    public ApiResponse<?> createComment(
            @PathVariable("tweetId") String tweetId,
            @RequestBody CommentRequest request
    ) {
        log.info("CommentService::createComment - Execution started for tweetId: {}", tweetId);
        CommentResponse comment = commentService.createComment(tweetId, null, request);
        log.info("CommentService::createComment - Execution ended for tweetId: {}", tweetId);
        return new ApiResponse<>(HttpStatus.CREATED, "Comment created successfully", comment);
    }

    @PostMapping("/comments/{parentId}/replies")
    public ApiResponse<?> createRely(
            @PathVariable("parentId") String parentId,
            @RequestBody CommentRequest request
    ) {
        log.info("CommentService::createComment - Execution started for parentId: {}", parentId);
        CommentResponse comment = commentService.createComment(null, parentId, request);
        log.info("CommentService::createComment - Execution ended");
        return new ApiResponse<>(HttpStatus.CREATED, "Comment created successfully", comment);
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<?> deleteComment(
            @PathVariable("commentId") String commentId
    ) {
        log.info("CommentService::deleteComment - Execution started for commentId: {}", commentId);
        commentService.deleteComment(commentId);
        log.info("CommentService::deleteComment - Execution ended for commentId: {}", commentId);
        return new ApiResponse<>(HttpStatus.NO_CONTENT, "Comment deleted successfully");
    }
}
