package com.david.common.dto.profile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse implements Serializable {

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

    private LocalDate dateOfBirth;

    private LocalDate joinDate;
}
