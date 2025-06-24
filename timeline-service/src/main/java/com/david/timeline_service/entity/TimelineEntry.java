package com.david.timeline_service.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "timeline_entries")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimelineEntry {

    @Id
    private String id;

    private String userId;

    private String tweetId;

    private String tweetOwnerId;

    private long tweetAt;

    @CreatedDate
    private long createdAt;
}
