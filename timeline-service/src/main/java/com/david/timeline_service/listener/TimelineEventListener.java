package com.david.timeline_service.listener;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.follow.FollowedEventPayload;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.timeline_service.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class TimelineEventListener {

    private final TimelineService timelineService;


    @RabbitListener(queues = "${app.rabbitmq.queue.followed}")
    public void handleFollowedEvent(@Payload ApiEventMessage<FollowedEventPayload> message) {
        log.info("Received followed event: {}", message.getEventId());
        try {
            if ("FOLLOWED".equals(message.getEventType())) {
                log.info("Processing followed event for followerId: {}, followedId: {}",
                        message.getPayload().getFollowerId(),
                        message.getPayload().getFollowedId()
                );
                timelineService.handleFollowed(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing followed event: {}", e.getMessage(), e);
        }
        log.info("Followed event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.unfollowed}")
    public void handleUnfollowedEvent(@Payload ApiEventMessage<FollowedEventPayload> message) {
        log.info("Received unfollowed event: {}", message.getEventId());
        try {
            if ("UNFOLLOWED".equals(message.getEventType())) {
                log.info("Processing unfollowed event for followerId: {}, followedId: {}",
                        message.getPayload().getFollowerId(),
                        message.getPayload().getFollowedId()
                );
                timelineService.handleUnfollowed(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing unfollowed event: {}", e.getMessage(), e);
        }
        log.info("Unfollowed event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.tweet-created}")
    public void handleNewTweetEvent(@Payload ApiEventMessage<TweetCreatedEventPayload> message) {
        log.info("Received new tweet event: {}", message.getEventId());
        try {
            if ("TWEET_CREATED".equals(message.getEventType())) {
                log.info("Processing new tweet event for userId: {}, tweetId: {}",
                        message.getPayload().getUserId(),
                        message.getPayload().getId()
                );
                timelineService.handleNewTweet(message.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing new tweet event: {}", e.getMessage(), e);
        }
        log.info("New tweet event processed successfully: {}", message.getEventId());
    }
}
