package com.david.notification_service.listener;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.comment.CommentCreatedEventPayload;
import com.david.common.dto.follow.FollowedEventPayload;
import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.common.dto.tweet.TweetLikedEventPayload;
import com.david.notification_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @RabbitListener(queues = "${app.rabbitmq.queue.user-registered}")
    public void handleUserRegisteredEvent(@Payload ApiEventMessage<ProfileCreatedEventPayload> message) {
        log.info("Received user registered event: {}", message.getEventId());
        try {
            if ("REGISTER".equals(message.getEventType())) {
                log.info("Processing user registration for userId: {}, username: {}, email: {}",
                        message.getPayload().getUserId(),
                        message.getPayload().getUsername(),
                        message.getPayload().getEmail()
                );
                notificationService.handleUserRegistered(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing user registered event: {}", e.getMessage(), e);
        }
        log.info("User registered event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.followed}")
    public void handleFollowedEvent(@Payload ApiEventMessage<FollowedEventPayload> message) {
        log.info("Received followed event: {}", message.getEventId());
        try {
            if ("FOLLOWED".equals(message.getEventType())) {
                log.info("Processing followed event for followerId: {}, followedId: {}",
                        message.getPayload().getFollowerId(),
                        message.getPayload().getFollowedId()
                );
                notificationService.handleFollowed(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing followed event: {}", e.getMessage(), e);
        }
        log.info("Followed event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.tweet-created}")
    public void handleTweetCreatedEvent(@Payload ApiEventMessage<TweetCreatedEventPayload> message) {
        log.info("Received tweet created event: {}", message.getEventId());
        try {
            if ("TWEET_CREATED".equals(message.getEventType())) {
                log.info("Processing tweet created event for tweetId: {}", message.getPayload());
                notificationService.handleTweetCreated(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing tweet created event: {}", e.getMessage(), e);
        }
        log.info("Tweet created event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.tweet-liked}")
    public void handleTweetLikedEvent(@Payload ApiEventMessage<TweetLikedEventPayload> message) {
        log.info("Received tweet liked event: {}", message.getEventId());
        try {
            if ("TWEET_LIKED".equals(message.getEventType())) {
                log.info("Processing tweet liked event for tweetId: {}", message.getPayload().getTweetId());
                notificationService.handleTweetLiked(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing tweet liked event: {}", e.getMessage(), e);
        }
        log.info("Tweet liked event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.comment-created}")
    public void handleCommentCreatedEvent(@Payload ApiEventMessage<CommentCreatedEventPayload> message) {
        log.info("Received comment created event: {}", message.getEventId());
        try {
            if ("COMMENT_CREATED".equals(message.getEventType())) {
                log.info("Processing comment created event for tweetId: {}, commentId: {}",
                        message.getPayload().getTweetId(),
                        message.getPayload().getCommentId()
                );
                notificationService.handleCommentCreated(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing comment created event: {}", e.getMessage(), e);
        }
    }
}
