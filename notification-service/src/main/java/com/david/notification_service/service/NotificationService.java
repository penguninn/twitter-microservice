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
import com.david.notification_service.entity.FcmToken;
import com.david.notification_service.entity.Notification;
import com.david.notification_service.enums.TypeNotification;
import com.david.notification_service.exception.NotificationServiceException;
import com.david.notification_service.repository.*;
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
    private final FcmTokenService fcmTokenService;
    private final FcmTokenRepository fcmTokenRepository;
    private final EmailService emailService;

    public void handleUserRegistered(ProfileCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleUserRegistered - Execution started");

            if (payload == null || payload.getUserId() == null || payload.getUsername() == null) {
                log.warn("NotificationService::handleUserRegistered - Invalid payload");
                return;
            }

            Notification notification = Notification.builder()
                    .userId(payload.getUserId())
                    .senderId(payload.getUserId())
                    .type(TypeNotification.WELCOME)
                    .message("Welcome " + payload.getUsername() + "! Your account has been successfully created.")
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();

            notificationRepository.save(notification);
            sendFcmNotificationToUser(payload.getUserId(), notification);
            emailService.sendEmail(payload.getEmail(), "Welcome to Our Service",
                    "Hello " + payload.getUsername() + ",\n\n" +
                            "Welcome to our service! Your account has been successfully created.\n\n" +
                            "Best regards,\n" +
                            "The Team");
            log.info("NotificationService::handleUserRegistered - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::handleUserRegistered - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to register account: " + e.getMessage());
        }
    }

    public void handleFollowed(FollowedEventPayload payload) {
        try {
            log.info("NotificationService::handleFollowed - Execution started");

            if (payload == null || payload.getFollowedId() == null || payload.getFollowerId() == null) {
                log.warn("NotificationService::handleFollowed - Invalid payload");
                return;
            }

            if (payload.getFollowedId().equals(payload.getFollowerId())) {
                log.debug("NotificationService::handleFollowed - User following themselves, skipping notification");
                return;
            }

            Notification notification = Notification.builder()
                    .userId(payload.getFollowedId())
                    .senderId(payload.getFollowerId())
                    .type(TypeNotification.FOLLOW)
                    .message("You have a new follower: " + payload.getFollowerId())
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();

            notificationRepository.save(notification);
            sendFcmNotificationToUser(payload.getFollowedId(), notification);

            log.info("NotificationService::handleFollowed - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::handleFollowed - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to handle followed event: " + e.getMessage());
        }
    }

    public void handleTweetCreated(TweetCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleTweetCreated - Execution started");

            if (payload == null || payload.getUserId() == null) {
                log.warn("NotificationService::handleTweetCreated - Invalid payload");
                return;
            }

            FeignApiResponse<PageResponse<List<FollowResponse>>> followClientFollowers = followClient.getFollowers(
                    payload.getUserId(), 1, 1000, "createdAt,desc"
            );

            if (followClientFollowers == null || followClientFollowers.getResult() == null ||
                    followClientFollowers.getResult().getContents() == null ||
                    followClientFollowers.getResult().getContents().isEmpty()) {
                log.info("NotificationService::handleTweetCreated - No followers found for userId: {}", payload.getUserId());
                return;
            } else {
                log.info("NotificationService::handleTweetCreated - Found {} followers for userId: {}",
                        followClientFollowers.getResult().getTotalElements(), payload.getUserId());
            }

            List<Notification> notifications = followClientFollowers.getResult().getContents().stream()
                    .map(follower -> Notification.builder()
                            .userId(follower.getFollowerId())
                            .senderId(payload.getUserId())
                            .type(TypeNotification.TWEET)
                            .message("User " + payload.getUserId() + " just posted a new tweet!")
                            .read(false)
                            .createdAt(String.valueOf(System.currentTimeMillis()))
                            .build()
                    ).toList();

            if (notifications.isEmpty()) {
                log.info("NotificationService::handleTweetCreated - No valid notifications to create");
                return;
            }

            List<Notification> savedNotifications = notificationRepository.saveAll(notifications);
            sendFcmNotifications(savedNotifications);
            log.info("NotificationService::handleTweetCreated - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::handleTweetCreated - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to handle tweet created event: " + e.getMessage());
        }
    }

    public void handleTweetLiked(TweetLikedEventPayload payload) {
        try {
            log.info("NotificationService::handleTweetLiked - Execution started");

            if (payload == null || payload.getTweetId() == null || payload.getUserId() == null) {
                log.warn("NotificationService::handleTweetLiked - Invalid payload");
                return;
            }

            FeignApiResponse<TweetResponse> tweetResponse = tweetClient.getTweetById(payload.getTweetId());
            if (tweetResponse == null || tweetResponse.getResult() == null) {
                log.warn("NotificationService::handleTweetLiked - Tweet not found: {}", payload.getTweetId());
                return;
            }

            String tweetOwnerId = tweetResponse.getResult().getUserId();

            if (tweetOwnerId.equals(payload.getUserId())) {
                log.debug("NotificationService::handleTweetLiked - User liked their own tweet, skipping notification");
                return;
            }

            Notification notification = Notification.builder()
                    .userId(tweetOwnerId)
                    .senderId(payload.getUserId())
                    .type(TypeNotification.LIKE)
                    .message("Your tweet has been liked by: " + payload.getUserId())
                    .read(false)
                    .createdAt(String.valueOf(System.currentTimeMillis()))
                    .build();

            notificationRepository.save(notification);
            sendFcmNotificationToUser(tweetOwnerId, notification);
            log.info("NotificationService::handleTweetLiked - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::handleTweetLiked - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to handle tweet liked event: " + e.getMessage());
        }
    }

    public void handleCommentCreated(CommentCreatedEventPayload payload) {
        try {
            log.info("NotificationService::handleCommentCreated - Execution started");

            if (payload == null || payload.getTweetId() == null || payload.getUserId() == null) {
                log.warn("NotificationService::handleCommentCreated - Invalid payload");
                return;
            }

            FeignApiResponse<TweetResponse> tweetResponse = tweetClient.getTweetById(payload.getTweetId());
            if (tweetResponse == null || tweetResponse.getResult() == null) {
                log.warn("NotificationService::handleCommentCreated - Tweet not found: {}", payload.getTweetId());
                return;
            }

            String tweetOwnerId = tweetResponse.getResult().getUserId();

            if (!tweetOwnerId.equals(payload.getUserId()) && payload.getParentId() == null) {
                Notification notiForTweetOwner = Notification.builder()
                        .userId(tweetOwnerId)
                        .senderId(payload.getUserId())
                        .type(TypeNotification.COMMENT)
                        .message("User " + payload.getUserId() + " commented on your tweet.")
                        .read(false)
                        .createdAt(String.valueOf(System.currentTimeMillis()))
                        .build();

                notificationRepository.save(notiForTweetOwner);
                sendFcmNotificationToUser(tweetOwnerId, notiForTweetOwner);
            }

            if (payload.getParentId() != null) {
                FeignApiResponse<CommentResponse> parentComment = commentClient.getCommentById(payload.getParentId());
                if (parentComment != null && parentComment.getResult() != null) {
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
                        sendFcmNotificationToUser(parentCommentOwnerId, notiForParentCommentOwner);
                    }
                }
            }

            log.info("NotificationService::handleCommentCreated - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::handleCommentCreated - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to handle comment created event: " + e.getMessage());
        }
    }

    public PageResponse<?> getNotifications(int page, int size, String sortBy) {
        try {
            log.info("NotificationService::getNotifications - Execution started");

            String userId = getCurrentUserId();
            String[] sortParams = sortBy.split(",");
            Sort sort = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(Math.max(0, page - 1), size, sort);
            Page<Notification> notifications = notificationRepository.findByUserId(userId, pageable);

            if (notifications.isEmpty()) {
                log.info("NotificationService::getNotifications - No notifications found for userId: {}", userId);
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
            log.error("NotificationService::getNotifications - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to fetch notifications: " + e.getMessage());
        }
    }

    public long countUnreadNotifications() {
        try {
            log.info("NotificationService::countUnreadNotifications - Execution started");

            String userId = getCurrentUserId();
            long count = notificationRepository.countByUserIdAndRead(userId, false);

            log.info("NotificationService::countUnreadNotifications - Execution ended successfully with count: {}", count);
            return count;
        } catch (Exception e) {
            log.error("NotificationService::countUnreadNotifications - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to count unread notifications: " + e.getMessage());
        }
    }

    public void markNotificationAsRead(String notificationId) {
        try {
            log.info("NotificationService::markNotificationAsRead - Execution started for notificationId: {}", notificationId);

            if (notificationId == null || notificationId.trim().isEmpty()) {
                log.warn("NotificationService::markNotificationAsRead - Invalid notificationId");
                throw new NotificationServiceException("Notification ID cannot be null or empty");
            }

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationServiceException("Notification not found"));
            notification.setRead(true);
            notificationRepository.save(notification);

            log.info("NotificationService::markNotificationAsRead - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::markNotificationAsRead - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to mark notification as read: " + e.getMessage());
        }
    }

    public void markAllNotificationsAsRead() {
        try {
            log.info("NotificationService::markAllNotificationsAsRead - Execution started");

            String userId = getCurrentUserId();

            int updatedCount = notificationRepository.markAllAsReadByUserId(userId);

            log.info("NotificationService::markAllNotificationsAsRead - Execution ended successfully, updated {} notifications", updatedCount);
        } catch (Exception e) {
            log.error("NotificationService::markAllNotificationsAsRead - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to mark all notifications as read: " + e.getMessage());
        }
    }

    public void deleteNotification(String notificationId) {
        try {
            log.info("NotificationService::deleteNotification - Execution started for notificationId: {}", notificationId);

            if (notificationId == null || notificationId.trim().isEmpty()) {
                log.warn("NotificationService::deleteNotification - Invalid notificationId");
                throw new NotificationServiceException("Notification ID cannot be null or empty");
            }

            Notification notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new NotificationServiceException("Notification not found"));
            notificationRepository.delete(notification);

            log.info("NotificationService::deleteNotification - Execution ended successfully");
        } catch (Exception e) {
            log.error("NotificationService::deleteNotification - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to delete notification: " + e.getMessage());
        }
    }

    public void deleteNotifications(Boolean read) {
        try {
            log.info("NotificationService::deleteNotifications - Execution started with read = {}", read);

            String userId = getCurrentUserId();

            int deletedCount;
            if (read != null) {
                deletedCount = notificationRepository.deleteByUserIdAndRead(userId, read);
            } else {
                deletedCount = notificationRepository.deleteByUserId(userId);
            }

            log.info("NotificationService::deleteNotifications - Execution ended successfully, deleted {} notifications", deletedCount);
        } catch (Exception e) {
            log.error("NotificationService::deleteNotifications - Execution ended with error: {}", e.getMessage(), e);
            throw new NotificationServiceException("Failed to delete notifications: " + e.getMessage());
        }
    }

    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            log.warn("NotificationService::getCurrentUserId - User is not authenticated");
            throw new AuthenticationCredentialsNotFoundException("Unauthorized access");
        }
        return jwt.getSubject();
    }

    private void sendFcmNotificationToUser(String userId, Notification notification) {
        try {
            List<FcmToken> fcmTokens = fcmTokenRepository.findAllByUserId(userId);

            if (fcmTokens.isEmpty()) {
                log.debug("NotificationService::sendFcmNotificationToUser - No FCM tokens found for userId: {}", userId);
                return;
            }

            for (FcmToken fcmToken : fcmTokens) {
                try {
                    fcmTokenService.sendPushNotification(fcmToken, notification);
                } catch (Exception e) {
                    log.warn("NotificationService::sendFcmNotificationToUser - Failed to send notification to userId: {}, error: {}",
                            userId, e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("NotificationService::sendFcmNotificationToUser - Error sending FCM notification to userId: {}, error: {}",
                    userId, e.getMessage(), e);
        }
    }

    private void sendFcmNotifications(List<Notification> notifications) {
        int successCount = 0;
        int failureCount = 0;

        for (Notification notification : notifications) {
            try {
                List<FcmToken> fcmTokens = fcmTokenRepository.findAllByUserId(notification.getUserId());

                if (fcmTokens.isEmpty()) {
                    log.debug("NotificationService::sendFcmNotifications - No FCM tokens found for userId: {}",
                            notification.getUserId());
                    continue;
                }

                for (FcmToken fcmToken : fcmTokens) {
                    try {
                        fcmTokenService.sendPushNotification(fcmToken, notification);
                        successCount++;
                    } catch (Exception e) {
                        log.warn("NotificationService::sendFcmNotifications - Failed to send notification to userId: {}, error: {}",
                                notification.getUserId(), e.getMessage());
                        failureCount++;
                    }
                }
            } catch (Exception e) {
                log.error("NotificationService::sendFcmNotifications - Error processing notification for userId: {}, error: {}",
                        notification.getUserId(), e.getMessage(), e);
                failureCount++;
            }
        }

        log.info("NotificationService::sendFcmNotifications - FCM notifications sent. Success: {}, Failures: {}",
                successCount, failureCount);
    }
}