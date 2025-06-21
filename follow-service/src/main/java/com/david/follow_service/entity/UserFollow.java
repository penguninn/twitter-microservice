package com.david.follow_service.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_follows")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserFollow {

    @Id
    private String id;

    private String followerId;

    private String followedId;

    @CreatedDate
    private Long createdAt;
}
