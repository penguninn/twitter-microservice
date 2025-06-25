package com.david.notification_service.repository;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.comment.CommentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "comment-service", url = "${app.services.comment-service.url}")
public interface CommentClient {

    @GetMapping("/api/v1/comments/{commentId}")
    FeignApiResponse<CommentResponse> getCommentById(@PathVariable("commentId") String tweetId);
}
