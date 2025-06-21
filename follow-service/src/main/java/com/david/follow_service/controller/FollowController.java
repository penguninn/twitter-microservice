package com.david.follow_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.follow_service.service.FollowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follows")
@Slf4j
public class FollowController {

    private final FollowService followService;

    @PostMapping("/{followedId}")
    public ApiResponse<?> followUser(@PathVariable String followedId) {
        log.info("FollowController::followUser - Attempting to follow user with ID: {}", followedId);
        followService.followUser(followedId);
        log.info("FollowController::followUser - User followed successfully: {}", followedId);
        return new ApiResponse<>(HttpStatus.CREATED, "User followed successfully");
    }

    @DeleteMapping("/{followedId}")
    public ApiResponse<?> unfollowUser(@PathVariable String followedId) {
        log.info("FollowController::unfollowUser - Attempting to unfollow user with ID: {}", followedId);
        followService.unFollowUser(followedId);
        log.info("FollowController::unfollowUser - User unfollowed successfully: {}", followedId);
        return new ApiResponse<>(HttpStatus.OK, "User unfollowed successfully");
    }

    @GetMapping("/{followerId}")
    public ApiResponse<?> isFollowing(@PathVariable String followerId) {
        log.info("FollowController::isFollowing - Checking if user with ID {} is following", followerId);
        boolean isFollowing = followService.isFollowing(followerId);
        log.info("FollowController::isFollowing - User with ID {} is following: {}", followerId, isFollowing);
        return new ApiResponse<>(HttpStatus.OK, "No message", isFollowing);
    }

    @GetMapping("/{followedId}/followers")
    public ApiResponse<?> getFollowers(
            @PathVariable String followedId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy) {
        log.info("FollowController::getFollowers - Fetching followers for user with ID: {}", followedId);
        var response = followService.getFollowers(followedId, page, size, sortBy);
        log.info("FollowController::getFollowers - Followers fetched successfully for user ID: {}", followedId);
        return new ApiResponse<>(HttpStatus.OK, "Followers fetched successfully", response);
    }

    @GetMapping("/{followerId}/following")
    public ApiResponse<?> getFollowing(
            @PathVariable String followerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sortBy) {
        log.info("FollowController::getFollowing - Fetching following for user with ID: {}", followerId);
        var response = followService.getFollowing(followerId, page, size, sortBy);
        log.info("FollowController::getFollowing - Following fetched successfully for user ID: {}", followerId);
        return new ApiResponse<>(HttpStatus.OK, "Following fetched successfully", response);
    }
}
