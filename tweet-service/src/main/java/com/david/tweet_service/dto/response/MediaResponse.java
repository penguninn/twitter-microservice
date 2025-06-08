package com.david.tweet_service.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaResponse {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
