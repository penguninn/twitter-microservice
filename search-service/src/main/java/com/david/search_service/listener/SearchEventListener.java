package com.david.search_service.listener;

import com.david.common.dto.ApiEventMessage;
import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.david.common.dto.profile.ProfileUpdatedEventPayload;
import com.david.common.dto.tweet.TweetCreatedEventPayload;
import com.david.search_service.document.TweetDocument;
import com.david.search_service.document.UserDocument;
import com.david.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchEventListener {
    private final SearchService searchService;

    @RabbitListener(queues = "${app.rabbitmq.queue.search-tweet-created}")
    public void onTweetCreated(@Payload ApiEventMessage<TweetCreatedEventPayload> message) {
        TweetCreatedEventPayload event = message.getPayload();
        log.info("Received TweetCreatedEvent in SearchService: tweetId={}", event.getId());
        try {
            TweetDocument doc = new TweetDocument();
            doc.setTweetId(event.getId());
            doc.setUserId(event.getUserId());
            doc.setContent(event.getContent());
            doc.setHashtags(event.getHashtags());
            doc.setVisibility(event.getVisibility());
            doc.setCreatedAt(event.getCreatedAt());
            doc.setUpdatedAt(event.getCreatedAt());
            searchService.indexTweet(doc);
        } catch (Exception e) {
            log.error("Error processing TweetCreatedEvent for indexing: {}", event, e);
        }
    }

//    @RabbitListener(queues = "${app.rabbitmq.queue.search-tweet-updated:search.tweet.updated.queue}")
//    public void onTweetUpdated(TweetUpdatedEvent event) { // NEW Listener
//        log.info("Received TweetUpdatedEvent in SearchService: tweetId={}", event.getTweetId());
//        try {
//            TweetDocument doc = new TweetDocument();
//            doc.setId(event.getTweetId());
//            doc.setUserId(event.getUserId());
//            doc.setContent(event.getContent());
//            doc.setHashtags(event.getHashtags());
//            doc.setVisibility(event.getVisibility() != null ? event.getVisibility().toUpperCase() : "PUBLIC");
//            // CreatedAt should ideally not change on update, load existing doc or make event richer
//            // doc.setCreatedAt(event.getCreatedAt()); // This might be wrong if event doesn't carry original createdAt
//            doc.setUpdatedAt(event.getUpdatedAt());
//            searchService.indexTweet(doc); // save acts as upsert
//        } catch (Exception e) {
//            log.error("Error processing TweetUpdatedEvent for indexing: {}", event, e);
//        }
//    }

    @RabbitListener(queues = "${app.rabbitmq.queue.search-tweet-deleted}")
    public void onTweetDeleted(@Payload ApiEventMessage<String> message) {
        String tweetId = message.getPayload();
        log.info("Received TweetDeletedEvent in SearchService: tweetId={}", tweetId);
        try {
            searchService.deleteTweetFromIndex(tweetId);
        } catch (Exception e) {
            log.error("Error processing TweetDeletedEvent for de-indexing", e);
        }
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.user-registered}")
    public void onProfileCreated(@Payload ApiEventMessage<ProfileCreatedEventPayload> message) {
        log.info("Received profile created event: {}", message.getEventId());
        try {
            if ("REGISTER".equals(message.getEventType())) {
                ProfileCreatedEventPayload event = message.getPayload();
                log.info("Processing profile creation for userId: {}, username: {}, email: {}",
                        event.getUserId(),
                        event.getUsername(),
                        event.getEmail()
                );
                
                UserDocument doc = new UserDocument();
                doc.setUserId(event.getUserId());
                doc.setUsername(event.getUsername());
                doc.setDisplayName(event.getDisplayName() != null ? event.getDisplayName() : event.getUsername());
                doc.setBio(null); // Bio is empty for new users
                doc.setProfileImageUrl(event.getProfileImageUrl());
                
                searchService.indexUser(doc);
                log.info("Profile indexed successfully for userId: {}", event.getUserId());
            }
        } catch (Exception e) {
            log.error("Error processing profile created event: {}", e.getMessage(), e);
        }
        log.info("Profile created event processed successfully: {}", message.getEventId());
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.user-registered}")
    public void onProfileUpdated(@Payload ApiEventMessage<ProfileUpdatedEventPayload> message) {
        ProfileUpdatedEventPayload event = message.getPayload();
        log.info("Received UserProfileUpdatedEvent in SearchService: userId={}", event.getUserId());
        try {
            UserDocument doc = new UserDocument();
            doc.setUserId(event.getUserId());
            doc.setDisplayName(event.getDisplayName());
            doc.setUsername(event.getUsername());
            doc.setUsername(event.getUsername());
            doc.setBio(event.getBio());
            doc.setProfileImageUrl(event.getProfileImageUrl());
            searchService.indexUser(doc);
        } catch (Exception e) {
            log.error("Error processing UserProfileUpdatedEvent for indexing: {}", event, e);
        }
    }
}