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
    public void handleFollowedEvent(@Payload ApiEventMessage<FollowedEventPayload> payload) {
        log.info("Received followed event: {}", payload.getEventId());
        try {
            if ("FOLLOWED".equals(payload.getEventType())) {
                log.info("Processing followed event for followerId: {}, followedId: {}",
                        payload.getPayload().getFollowerId(),
                        payload.getPayload().getFollowedId()
                );
                timelineService.handleFollowed(payload.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing followed event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.unfollowed}")
    public void handleUnfollowedEvent(@Payload ApiEventMessage<FollowedEventPayload> payload) {
        log.info("Received unfollowed event: {}", payload.getEventId());
        try {
            if ("UNFOLLOWED".equals(payload.getEventType())) {
                log.info("Processing unfollowed event for followerId: {}, followedId: {}",
                        payload.getPayload().getFollowerId(),
                        payload.getPayload().getFollowedId()
                );
                timelineService.handleUnfollowed(payload.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing unfollowed event: {}", e.getMessage(), e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.tweet-created}")
    public void handleNewTweetEvent(@Payload ApiEventMessage<TweetCreatedEventPayload> payload) {
        log.info("Received new tweet event: {}", payload.getEventId());
        try {
            if ("TWEET_CREATED".equals(payload.getEventType())) {
                log.info("Processing new tweet event for userId: {}, tweetId: {}",
                        payload.getPayload().getUserId(),
                        payload.getPayload().getTweetId()
                );
                timelineService.handleNewTweet(payload.getPayload());
            }
        } catch (Exception e) {
            log.error("Error processing new tweet event: {}", e.getMessage(), e);
        }
    }
}
