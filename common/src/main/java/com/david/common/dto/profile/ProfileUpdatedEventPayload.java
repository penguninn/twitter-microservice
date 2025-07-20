package com.david.common.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileUpdatedEventPayload implements Serializable {

    private String userId;

    private String username;

    private String email;

    private String displayName;

    private boolean gender;

    private String bio;

    private String location;

    private String websiteUrl;

    private String profileImageUrl;

    private String bannerImageUrl;

    private LocalDate joinDate;

    private LocalDate dateOfBirth;

}
