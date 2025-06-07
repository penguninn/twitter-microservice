package com.david.media_service.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.MediaType;

@Getter
@Builder
public class MediaResponse {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
