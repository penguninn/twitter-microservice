package com.david.tweet_service.entity;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Media {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
