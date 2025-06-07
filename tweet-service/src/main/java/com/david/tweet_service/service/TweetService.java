package com.david.tweet_service.service;

import com.david.tweet_service.dto.request.TweetRequest;
import com.david.tweet_service.dto.response.FeignApiResponse;
import com.david.tweet_service.dto.response.MediaResponse;
import com.david.tweet_service.dto.response.TweetResponse;
import com.david.tweet_service.entity.Tweet;
import com.david.tweet_service.enums.Visibility;
import com.david.tweet_service.exception.TweetNotFoundException;
import com.david.tweet_service.exception.TweetServiceException;
import com.david.tweet_service.mapper.TweetMapper;
import com.david.tweet_service.repository.MediaClient;
import com.david.tweet_service.repository.TweetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TweetService {

    private final TweetRepository tweetRepository;
    private final MediaClient mediaClient;


//    @Cacheable(value = "cacheTweet", key = "#tweetId")
    public TweetResponse getTweetById(String tweetId) {
        log.info("TweetService::getTweetById - Execution started for tweetId: {}", tweetId);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Tweet not found with id: " + tweetId));

        if(tweet.getVisibility() == Visibility.PRIVATE) {
            if(authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
                String userId = jwt.getSubject();
                if (!tweet.getUserId().equals(userId)) {
                    log.warn("TweetService::getTweetById - User {} is not authorized to view tweet {}", userId, tweetId);
                    throw new TweetNotFoundException("Tweet not found with id: " + tweetId);
                }
            } else {
                log.warn("TweetService::getTweetById - Unauthorized access attempt for tweetId: {}", tweetId);
                throw new TweetNotFoundException("Tweet not found with id: " + tweetId);
            }
        }
        log.info("TweetService::getTweetById - Execution ended for tweetId: {}", tweetId);
        return TweetMapper.mapToDto(tweet);
    }

    @PreAuthorize("#userId == authentication.principal.subject or hasRole('ROLE_ADMIN')")
    public List<TweetResponse> getMyTweets(String userId, int page, int size, String sortBy) {
        log.info("TweetService::getMyTweets - Execution started for userId: {}", userId);
        int p = Math.max(0, page - 1);
        String[] sortParams = sortBy.split(",");
        Sort.Direction direction = sortParams.length > 1 ? Sort.Direction.fromString(sortParams[1]) : Sort.Direction.ASC;
        Sort sortOrder = Sort.by(direction, sortParams[0]);
        Pageable pageable = PageRequest.of(p, size, sortOrder);
        List<TweetResponse> tweetResponses = tweetRepository.findAllByUserId(userId, pageable).stream()
                .map(TweetMapper::mapToDto)
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
                .map(TweetMapper::mapToDto)
                .toList();
        log.info("TweetService::getPublicTweets - Execution ended");
        return tweetResponses;
    }

    @Transactional
    @PreAuthorize("#userId == authentication.principal.subject or hasRole('ROLE_ADMIN')")
//    @CachePut(value = "cacheTweet", key = "#result.id")
    public TweetResponse createTweet(String userId, TweetRequest tweetRequest, MultipartFile[] mediaFiles) {
        log.info("TweetService::createTweet - Execution started for userId: {}", userId);
        Tweet tweet = TweetMapper.mapToEntity(tweetRequest);
        List<MediaResponse> mediaList = new ArrayList<>();
        if (mediaFiles != null && mediaFiles.length > 0) {
            log.info("TweetService::createTweet - Processing media files for tweet");
            try {
                FeignApiResponse<List<MediaResponse>> mediaResponse = mediaClient.uploadFiles(mediaFiles, "MEDIA");
                if (mediaResponse != null) {
                    mediaList = mediaResponse.getResult();
                    log.info("ProfileService::createTweet - Media files uploaded successfully");
                }
                tweet.setMedia(mediaList);
            } catch (Exception e) {
                log.error("TweetService::createTweet - Exception occurred while uploading media files: {}", e.getMessage());
                throw new TweetServiceException("Failed to upload media files" + e.getMessage());
            }
        }
        tweet.setUserId(userId);
        Tweet savedTweet = tweetRepository.save(tweet);
        log.info("TweetService::createTweet - Tweet saved with id: {}", savedTweet.getId());

        log.info("TweetService::createTweet - Execution ended");
        return TweetMapper.mapToDto(savedTweet);
    }

    @Transactional
    @PreAuthorize("#userId == authentication.principal.subject or hasRole('ROLE_ADMIN')")
//    @CachePut(value = "cacheTweet", key = "#tweetId")
    public TweetResponse toggleLikeTweet(String tweetId, String userId) {
        log.info("TweetService::toggleLikeTweet - Execution started for tweetId: {}, userId: {}", tweetId, userId);
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Tweet not found with id: " + tweetId));
        List<String> likedBy = tweet.getLikedBy();
        boolean isLiked;
        if (likedBy.contains(userId)) {
            likedBy.remove(userId);
            tweet.getStats().setLikesCount(Math.max(0, tweet.getStats().getLikesCount() - 1));
            isLiked = false;
            log.info("TweetService::toggleLikeTweet - User {} unliked tweet {}", userId, tweetId);
        } else {
            likedBy.add(userId);
            tweet.getStats().setLikesCount(tweet.getStats().getLikesCount() + 1);
            isLiked = true;
            log.info("TweetService::toggleLikeTweet - User {} liked tweet {}", userId, tweetId);
        }
        log.info("TweetService::toggleLikeTweet - Execution ended");
        return TweetMapper.mapToDto(tweetRepository.save(tweet));
    }

    @Transactional
    @PreAuthorize("#userId == authentication.principal.subject or hasRole('ROLE_ADMIN')")
//    @CacheEvict(value = "cacheTweet", key = "#tweetId")
    public void deleteTweet(String tweetId, String userId) {
        log.info("TweetService::deleteTweet - Execution started for tweetId: {}, userId: {}", tweetId, userId);
        Tweet tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException("Tweet not found with id: " + tweetId));
        tweetRepository.delete(tweet);
        log.info("TweetService::deleteTweet - Tweet deleted with id: {}", tweetId);
    }
}
