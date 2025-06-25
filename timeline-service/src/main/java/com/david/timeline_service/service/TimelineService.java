package com.david.timeline_service.service;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.follow.FollowResponse;
import com.david.common.dto.follow.FollowedEventPayload;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.common.dto.tweet.TweetResponse;
import com.david.timeline_service.entity.TimelineEntry;
import com.david.timeline_service.repository.FollowClient;
import com.david.timeline_service.repository.TimelineRepository;
import com.david.timeline_service.repository.TweetClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimelineService {

    private final TimelineRepository timelineRepository;
    private final TweetClient tweetClient;
    private final FollowClient followClient;

    public void handleFollowed(FollowedEventPayload payload) {
        log.info("TimelineService::handleFollowed - Execution started for followerId: {}, followedId: {}",
                payload.getFollowerId(), payload.getFollowedId());
        FeignApiResponse<List<TweetResponse>> recentTweets = tweetClient.getPublicTweets(
                payload.getFollowedId(),
                1,
                3,
                "createdAt,desc"
        );
        if (recentTweets.getResult() == null || recentTweets.getResult().isEmpty()) {
            log.info("No recent tweets found for followedId: {}", payload.getFollowedId());
            return;
        }
        List<TimelineEntry> timelineEntries = recentTweets.getResult()
                .stream()
                .map(tweet -> TimelineEntry.builder()
                        .userId(payload.getFollowerId())
                        .tweetId(tweet.getId())
                        .tweetOwnerId(tweet.getUserId())
                        .tweetAt(tweet.getCreatedAt())
                        .build())
                .toList();
        timelineRepository.saveAll(timelineEntries);
        log.info("TimelineService::handleFollowed - Execution ended for followerId: {}, followedId: {}",
                payload.getFollowerId(), payload.getFollowedId());
    }

    public void handleUnfollowed(FollowedEventPayload payload) {
        log.info("TimelineService::handleUnfollowed - Execution started for followerId: {}, followedId: {}",
                payload.getFollowerId(), payload.getFollowedId());
        timelineRepository.deleteByUserIdAndTweetOwnerId(payload.getFollowerId(), payload.getFollowedId());
        log.info("TimelineService::handleUnfollowed - Execution ended for followerId: {}, followedId: {}",
                payload.getFollowerId(), payload.getFollowedId());
    }

    public void handleNewTweet(TweetCreatedEventPayload payload) {
        log.info("TimelineService::handleNewTweet - Execution started for userId: {}, tweetId: {}",
                payload.getUserId(), payload.getTweetId());
        FeignApiResponse<PageResponse<List<FollowResponse>>> followersResponse = followClient.getFollowers(
                payload.getUserId(),
                1,
                1000,
                "createdAt,desc"
        );
        if (followersResponse.getResult() == null || followersResponse.getResult().getSize() == 0) {
            log.info("No followers found for userId: {}", payload.getUserId());
            return;
        }
        List<String> followerIds = followersResponse.getResult().getContents().stream()
                .map(FollowResponse::getFollowerId)
                .toList();
        List<TimelineEntry> timelineEntries = followerIds.stream()
                .map(followerId -> TimelineEntry.builder()
                        .userId(followerId)
                        .tweetId(payload.getTweetId())
                        .tweetAt(payload.getCreatedAt())
                        .tweetOwnerId(payload.getUserId())
                        .build())
                .toList();
        timelineRepository.saveAll(timelineEntries);
        log.info("TimelineService::handleNewTweet - Execution ended for userId: {}, tweetId: {}",
                payload.getUserId(), payload.getTweetId());
    }

    public List<TweetResponse> getTimeline(int page, int size, String sortBy) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("TweetService::unlikeTweet - Unauthorized access attempt");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        String userId = jwt.getSubject();
        log.info("TimelineService::getTimeline - Fetching timeline for userId: {}, page: {}, size: {}, sortBy: {}",
                userId, page, size, sortBy);
        String[] sortParams = sortBy.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
        Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
        Page<TimelineEntry> timelineEntries = timelineRepository.findByUserId(userId, pageable);
        List<String> tweetIds = timelineEntries.stream()
                .map(TimelineEntry::getTweetId)
                .toList();
        FeignApiResponse<List<TweetResponse>> tweetsResponse = tweetClient.getTweetsByIds(tweetIds);
        log.info("TimelineService::getTimeline - Timeline fetched successfully for userId: {}", userId);
        return tweetsResponse.getResult();
    }
}
