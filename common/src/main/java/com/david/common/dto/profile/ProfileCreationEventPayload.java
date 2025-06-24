package com.david.common.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCreationEventPayload implements Serializable {

    private String userId;

    private String username;

    private String email;

    private String displayName;

    private String profileImageUrl;
}
