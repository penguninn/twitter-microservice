package com.david.tweet_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaResponse {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
