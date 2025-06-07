package com.david.tweet_service.entity;

import lombok.*;
import org.springframework.http.MediaType;

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
