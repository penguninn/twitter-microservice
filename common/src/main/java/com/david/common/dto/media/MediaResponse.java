package com.david.common.dto.media;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MediaResponse implements Serializable {

    private String mediaId;

    private String mediaType;

    private String mediaUrl;
}
