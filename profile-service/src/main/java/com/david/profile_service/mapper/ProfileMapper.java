package com.david.profile_service.mapper;

import com.david.profile_service.dto.request.ProfileCreationRequest;
import com.david.profile_service.dto.response.ProfileResponse;
import com.david.profile_service.entity.Profile;

public class ProfileMapper {

    public static Profile mapToEntity(ProfileCreationRequest request) {
        return Profile.builder()
                .email(request.getEmail())
                .build();
    }

    public static ProfileResponse mapToDto(Profile profile) {
        return ProfileResponse.builder()
                .userId(profile.getUserId())
                .username(profile.getUsername())
                .email(profile.getEmail())
                .displayName(profile.getDisplayName())
                .bio(profile.getBio())
                .location(profile.getLocation())
                .websiteUrl(profile.getWebsiteUrl())
                .profileImageUrl(profile.getProfileImageUrl())
                .bannerImageUrl(profile.getBannerImageUrl())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.isGender())
                .joinDate(profile.getJoinDate().toString())
                .build();
    }
}
