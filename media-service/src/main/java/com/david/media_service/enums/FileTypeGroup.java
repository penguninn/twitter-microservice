package com.david.media_service.enums;

import java.util.List;

public enum FileTypeGroup {

    IMAGE(List.of("image/")),
    VIDEO(List.of("video/")),
    MEDIA(List.of("image/", "video/"));

    private final List<String> allowedContentTypes;

    FileTypeGroup(List<String> allowedContentTypes) {
        this.allowedContentTypes = allowedContentTypes;
    }

    public List<String> getAllowedContentTypes() {
        return allowedContentTypes;
    }
}
