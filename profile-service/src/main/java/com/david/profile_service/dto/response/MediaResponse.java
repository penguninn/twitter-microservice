package com.david.profile_service.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaResponse {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
