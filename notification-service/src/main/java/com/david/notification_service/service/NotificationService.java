package com.david.notification_service.service;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.comment.CommentCreatedEventPayload;
import com.david.common.dto.comment.CommentResponse;
import com.david.common.dto.follow.FollowResponse;
import com.david.common.dto.follow.FollowedEventPayload;
import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.common.dto.tweet.TweetLikedEventPayload;
import com.david.common.dto.tweet.TweetResponse;
import com.david.notification_service.entity.Notification;
import com.david.notification_service.enums.TypeNotification;
import com.david.notification_service.exception.NotificationServiceException;
import com.david.notification_service.repository.CommentClient;
import com.david.notification_service.repository.FollowClient;
import com.david.notification_service.repository.NotificationRepository;
import com.david.notification_service.repository.TweetClient;
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
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final TweetClient tweetClient;
    private final FollowClient followClient;
    private final CommentClient commentClient;

    public void handleUserRegistered(ProfileCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleUserRegistered - Execution started");
            Notification notification = Notification.builder()
                    .userId(payload.getUserId())
                    .senderId(payload.getUserId())
                    .type(TypeNotification.WELCOME)
                    .message("Welcome " + payload.getUsername() + "! Your account has been successfully created.")
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();
            notificationRepository.save(notification);
            log.info("NotificationRepository::handleUserRegistered - Execution ended successfully");
        } catch (Exception e) {
            log.info("NotificationService::handleUserRegistered - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to register account: " + e.getMessage());
        }
    }

    public void handleFollowed(FollowedEventPayload payload) {
        try {
            log.info("NotificationService::handleFollowed - Execution started");
            Notification notification = Notification.builder()
                    .userId(payload.getFollowedId())
                    .senderId(payload.getFollowerId())
                    .type(TypeNotification.FOLLOW)
                    .message("You have a new follower: " + payload.getFollowerId())
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();
            notificationRepository.save(notification);
            log.info("NotificationRepository::handleFollowed - Execution ended successfully");
        } catch (Exception e) {
            log.info("NotificationService::handleFollowed - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to handle followed event: " + e.getMessage());
        }
    }

    public void handleTweetCreated(TweetCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleTweetCreated - Execution started");
            FeignApiResponse<PageResponse<List<FollowResponse>>> followClientFollowers = followClient.getFollowers(
                    payload.getUserId(), 1, 1000, "createdAt,desc"
            );
            if (followClientFollowers.getResult() == null || followClientFollowers.getResult().getContents().isEmpty()) {
                log.info("No followers found for userId: {}", payload.getUserId());
                return;
            }
            followClientFollowers.getResult().getContents().forEach(follower -> {
                Notification notification = Notification.builder()
                        .userId(follower.getFollowerId())
                        .senderId(payload.getUserId())
                        .type(TypeNotification.TWEET)
                        .message("User " + payload.getUserId() + " just posted a new tweet!")
                        .read(false)
                        .createdAt(String.valueOf(System.currentTimeMillis()))
                        .build();

                notificationRepository.save(notification);
            });
            log.info("NotificationRepository::handleTweetCreated - Execution ended successfully");
        } catch (Exception e) {
            log.info("NotificationService::handleTweetCreated - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to handle tweet created event: " + e.getMessage());
        }
    }

    public void handleTweetLiked(TweetLikedEventPayload payload) {
        try {
            log.info("NotificationService::handleTweetLiked - Execution started");
            FeignApiResponse<TweetResponse> tweetResponse = tweetClient.getTweetById(payload.getTweetId());
            Notification notification = Notification.builder()
                    .userId(tweetResponse.getResult().getUserId())
                    .senderId(payload.getUserId())
                    .type(TypeNotification.LIKE)
                    .message("Your tweet has been liked by: " + payload.getUserId())
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();
            notificationRepository.save(notification);
            log.info("NotificationRepository::handleTweetLiked - Execution ended successfully");
        } catch (Exception e) {
            log.info("NotificationService::handleTweetLiked - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to handle tweet liked event: " + e.getMessage());
        }
    }

    public void handleCommentCreated(CommentCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleCommentCreated - Execution started");
            FeignApiResponse<TweetResponse> tweetResponse = tweetClient.getTweetById(payload.getTweetId());
            String tweetOwnerId = tweetResponse.getResult().getUserId();

            if( !tweetOwnerId.equals(payload.getUserId()) && payload.getParentId() == null) {
                Notification notiForTweetOwner = Notification.builder()
                        .userId(tweetOwnerId)
                        .senderId(payload.getUserId())
                        .type(TypeNotification.COMMENT)
                        .message("User " + payload.getUserId() + " commented on your tweet.")
                        .read(false)
                        .createdAt(String.valueOf(System.currentTimeMillis()))
                        .build();
                notificationRepository.save(notiForTweetOwner);
            }
            if (payload.getParentId() != null) {
                FeignApiResponse<CommentResponse> parentComment = commentClient.getCommentById(payload.getParentId());
                String parentCommentOwnerId = parentComment.getResult().getUserId();

                if (!parentCommentOwnerId.equals(payload.getUserId()) && !parentCommentOwnerId.equals(tweetOwnerId)) {
                    Notification notiForParentCommentOwner = Notification.builder()
                            .userId(parentCommentOwnerId)
                            .senderId(payload.getUserId())
                            .type(TypeNotification.COMMENT)
                            .message("User " + payload.getUserId() + " replied to your comment.")
                            .read(false)
                            .createdAt(String.valueOf(System.currentTimeMillis()))
                            .build();
                    notificationRepository.save(notiForParentCommentOwner);
                }
            }
            log.info("NotificationRepository::handleCommentCreated - Execution ended successfully");
        } catch (Exception e) {
            log.info("NotificationService::handleCommentCreated - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to handle comment created event: " + e.getMessage());
        }
    }

    public PageResponse<?> getNotifications(int page, int size, String sortBy) {
        try {
            log.info("NotificationService::getNotifications - Execution started");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.warn("NotificationService::getNotifications - User is not authenticated");
                throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
            }
            String userId = jwt.getSubject();
            String[] sortParams = sortBy.split(",");
            Sort sort = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
            Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);
            if (notifications.isEmpty()) {
                log.info("No notifications found for userId: {}", userId);
                return PageResponse.builder()
                        .page(0)
                        .size(size)
                        .totalElements(0)
                        .totalPages(1)
                        .contents(List.of())
                        .build();
            }
            List<Notification> notificationList = notifications.stream()
                    .map(notification -> Notification.builder()
                            .id(notification.getId())
                            .userId(notification.getUserId())
                            .senderId(notification.getSenderId())
                            .type(notification.getType())
                            .message(notification.getMessage())
                            .read(notification.isRead())
                            .createdAt(notification.getCreatedAt())
                            .build())
                    .toList();
            log.info("NotificationService::getNotifications - Execution ended successfully");
            return PageResponse.builder()
                    .page(notifications.getNumber() + 1)
                    .size(notifications.getSize())
                    .totalElements(notifications.getTotalElements())
                    .totalPages(notifications.getTotalPages())
                    .contents(notificationList)
                    .build();
        } catch (Exception e) {
            log.error("NotificationService::getNotifications - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to fetch notifications: " + e.getMessage());
        }
    }

    public long countUnreadNotifications() {
        try {
            log.info("NotificationService::countUnreadNotifications - Execution started");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.warn("NotificationService::countUnreadNotifications - User is not authenticated");
                throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
            }
            String userId = jwt.getSubject();
            long count = notificationRepository.countByUserIdAndRead(userId, false);
            log.info("NotificationService::countUnreadNotifications - Execution ended successfully with count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("NotificationService::countUnreadNotifications - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to count unread notifications: " + e.getMessage());
        }
    }

    public void markNotificationAsRead(String notificationId) {
        try {
            log.info("NotificationService::markNotificationAsRead - Execution started for notificationId: {}", notificationId);
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationServiceException("Notification not found"));
            notification.setRead(true);
            notificationRepository.save(notification);
            log.info("NotificationService::markNotificationAsRead - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::markNotificationAsRead - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to mark notification as read: " + e.getMessage());
        }
    }

    public void markAllNotificationsAsRead() {
        try {
            log.info("NotificationService::markAllNotificationsAsRead - Execution started");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.warn("NotificationService::markAllNotificationsAsRead - User is not authenticated");
                throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
            }
            String userId = jwt.getSubject();
            List<Notification> notifications = notificationRepository.findByUserId(userId, Pageable.unpaged()).getContent();
            notifications.forEach(notification -> notification.setRead(true));
            notificationRepository.saveAll(notifications);
            log.info("NotificationService::markAllNotificationsAsRead - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::markAllNotificationsAsRead - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to mark all notifications as read: " + e.getMessage());
        }
    }

    public void deleteNotification(String notificationId) {
        try {
            log.info("NotificationService::deleteNotification - Execution started for notificationId: {}", notificationId);
            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationServiceException("Notification not found"));
            notificationRepository.delete(notification);
            log.info("NotificationService::deleteNotification - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::deleteNotification - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to delete notification: " + e.getMessage());
        }
    }

    public void deleteNotifications(Boolean read) {
        try {
            log.info("NotificationService::deleteNotifications - Execution started with read = {}", read);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
                log.warn("NotificationService::deleteNotifications - User is not authenticated");
                throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
            }
            String userId = jwt.getSubject();
            List<Notification> notifications;
            if (read != null) {
                notifications = notificationRepository.findByUserIdAndRead(userId, read, Pageable.unpaged()).getContent();
            } else {
                notifications = notificationRepository.findByUserId(userId, Pageable.unpaged()).getContent();
            }
            notificationRepository.deleteAll(notifications);
            log.info("NotificationService::deleteNotifications - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::deleteNotifications - Execution ended with error: {}", e.getMessage());
            throw new NotificationServiceException("Failed to delete notifications: " + e.getMessage());
        }
    }

}
