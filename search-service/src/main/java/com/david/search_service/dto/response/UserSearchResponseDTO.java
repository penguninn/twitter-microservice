package com.david.search_service.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserSearchResponseDTO {

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
