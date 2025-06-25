package com.david.profile_service.service;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.media.MediaResponse;
import com.david.common.dto.profile.ProfileCreatedEventPayload;
import com.david.common.dto.profile.ProfileResponse;
import com.david.profile_service.dto.request.ChangePasswordRequest;
import com.david.profile_service.dto.request.EmailUpdateRequest;
import com.david.profile_service.dto.request.ProfileUpdateRequest;
import com.david.profile_service.dto.request.UsernameUpdateRequest;
import com.david.profile_service.entity.Profile;
import com.david.profile_service.exception.ProfileNotFoundException;
import com.david.profile_service.exception.ProfileServiceException;
import com.david.profile_service.mapper.ProfileMapper;
import com.david.profile_service.repository.MediaClient;
import com.david.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final MediaClient mediaClient;
    private final Keycloak keycloakAdminClient;
    private final CacheManager cacheManager;
    private final ProfileMapper profileMapper;

    @Value("${app.idp.realm}")
    private String realm;

    @Value("${app.idp.client-id}")
    private String clientId;

    @Value("${app.idp.url}")
    private String serverUrl;

    @Value("${app.idp.client-secret}")
    private String clientSecret;

    @Cacheable(cacheNames = "cacheProfileExists", key = "#userId")
    public boolean checkProfileExists(String userId) {
        log.info("ProfileService::checkProfileExists - Execution started. [userId: {}]", userId);
        boolean exists = profileRepository.existsByUserId(userId);
        log.info("ProfileService::checkProfileExists - Execution ended successfully. [userId: {}, exists: {}]", userId, exists);
        return exists;
    }

    @Cacheable(cacheNames = "cacheProfileById", key = "#userId")
    public ProfileResponse getProfileById(String userId) {
        log.info("ProfileService::getProfileById - Execution started. [userId: {}]", userId);
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("ProfileService::getProfileById - Profile not found for userId: {}", userId);
                    return new ProfileNotFoundException("Profile not found with userId: " + userId);
                });
        log.info("ProfileService::getProfileById - Execution ended successfully. [userId: {}]", userId);
        return profileMapper.toDto(profile);
    }

    @Cacheable(cacheNames = "cacheProfileByUsername", key = "#username")
    public ProfileResponse getProfileByUsername(String username) {
        log.info("ProfileService::getProfileByUsername - Execution started. [username: {}]", username);
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("ProfileService::getProfileByUsername - Profile not found for username: {}", username);
                    return new ProfileNotFoundException("Profile not found with username: " + username);
                });
        log.info("ProfileService::getProfileByUsername - Execution ended successfully. [username: {}]", username);
        return profileMapper.toDto(profile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PageResponse<?> getAllProfile(int page, int size, String sortBy) {
        log.info("ProfileService::getAllProfile - Execution started. [page: {}, size: {}, sortBy: {}]", page, size, sortBy);
        try {
            int p = Math.max(0, page - 1);
            String[] sortParams = sortBy.split(",");
            Sort.Direction direction = sortParams.length > 1 ? Sort.Direction.fromString(sortParams[1]) : Sort.Direction.ASC;
            Sort sortOrder = Sort.by(direction, sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Profile> profiles = profileRepository.findAll(pageable);
            List<ProfileResponse> profileResponses = profiles.stream()
                    .map(profileMapper::toDto)
                    .toList();
            log.info("ProfileService::getAllProfile - Execution ended successfully. Found {} profiles.", profileResponses.size());
            return PageResponse.builder()
                    .contents(profileResponses)
                    .page(page)
                    .size(size)
                    .totalPages(profiles.getTotalPages())
                    .totalElements(profiles.getTotalElements())
                    .build();
        } catch (IllegalArgumentException e) {
            log.error("ProfileService::getAllProfile - Invalid sort direction. [sortBy: {}]", sortBy, e);
            throw new ProfileServiceException("Invalid sort direction provided: " + sortBy, e);
        } catch (RuntimeException e) {
            log.error("ProfileService::getAllProfile - Failed to get all profiles.", e);
            throw new ProfileServiceException("Failed to get all profiles", e);
        }
    }

    @Transactional
    @CachePut(cacheNames = "cacheProfileById", key = "#result.userId")
    public Profile register(ProfileCreatedEventPayload request) {
        try {
            log.info("ProfileService::register - Execution started");
            Profile profile = Profile.builder()
                    .userId(request.getUserId())
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .displayName(request.getDisplayName())
                    .profileImageUrl(request.getProfileImageUrl())
                    .build();
            profileRepository.save(profile);
            log.info("ProfileService::register - Execution ended successfully");
            return profile;
        } catch (ProfileServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("ProfileService::register - Failed to register account. [email: {}]", request.getEmail(), e);
            throw new ProfileServiceException("Failed to register account: " + e.getMessage(), e);
        }
    }


    @Transactional
    @PreAuthorize("#    a0 == authentication.principal.subject or hasRole('ADMIN')")
    @Caching(
            put = {
                    @CachePut(cacheNames = "cacheProfileById", key = "#result.userId"),
                    @CachePut(cacheNames = "cacheProfileByUsername", key = "#result.username")
            }
    )
    public ProfileResponse updateUsername(String userId, UsernameUpdateRequest request) {
        log.info("ProfileService::updateUsername - Execution started. [userId: {}, newUsername: {}]", userId, request.getUsername());
        try {
            Profile existingProfile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warn("ProfileService::updateUsername - Profile not found in local DB. [userId: {}]", userId);
                        return new ProfileNotFoundException("Not found profile with user-id: " + userId);
                    });

            String oldUsername = existingProfile.getUsername();
            if (request.getUsername().equals(oldUsername)) {
                log.info("ProfileService::updateUsername - New username is the same as current. No update needed. [userId: {}]", userId);
                return profileMapper.toDto(existingProfile);
            }

            try {
                UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
                UserRepresentation userRepresentation = userResource.toRepresentation();
                log.info("ProfileService::updateUsername - Updating username in Keycloak. [userId: {}, oldUsernameKeycloak: {}, newUsername: {}]",
                        userId, userRepresentation.getUsername(), request.getUsername());
                userRepresentation.setUsername(request.getUsername());
                userResource.update(userRepresentation);
                log.info("ProfileService::updateUsername - Username updated successfully in Keycloak. [userId: {}, newUsername: {}]", userId, request.getUsername());
            } catch (jakarta.ws.rs.NotFoundException e) {
                log.error("ProfileService::updateUsername - User not found in Keycloak. [userId: {}]", userId, e);
                throw new ProfileNotFoundException("User not found in Keycloak with ID: " + userId, e);
            } catch (Exception e) {
                log.error("ProfileService::updateUsername - Keycloak error during username update. [userId: {}]", userId, e);
                throw new ProfileServiceException("Keycloak error during username update: " + e.getMessage(), e);
            }

            existingProfile.setUsername(request.getUsername());
            Profile updatedProfile = profileRepository.save(existingProfile);
            log.info("ProfileService::updateUsername - Username updated successfully in local DB. [userId: {}]", userId);

            if(oldUsername != null && !oldUsername.equals(request.getUsername())) {
                log.info("ProfileService::updateUsername - Updating cache for username: {}", oldUsername);
                cacheManager.getCache("cacheProfileByUsername").evict(oldUsername);
            }

            ProfileResponse updatedResponse = profileMapper.toDto(updatedProfile);
            log.info("ProfileService::updateUsername - Execution ended successfully. [userId: {}]", userId);
            return updatedResponse;
        } catch (ProfileServiceException | ProfileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("ProfileService::updateUsername - Failed to update username. [userId: {}]", userId, e);
            throw new ProfileServiceException("Failed to update username: " + e.getMessage(), e);
        }
    }

    @Transactional
    @PreAuthorize("#a0 == authentication.principal.subject or hasRole('ADMIN')")
    @CachePut(cacheNames = "cacheProfileById", key = "#result.userId")
    public ProfileResponse updateEmail(String userId, EmailUpdateRequest request) {
        log.info("ProfileService::updateEmail - Execution started. [userId: {}, newEmail: {}]", userId, request.getEmail());
        try {
            Profile existingProfile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warn("ProfileService::updateEmail - Profile not found in local DB. [userId: {}]", userId);
                        return new ProfileNotFoundException("Not found profile with user-id: " + userId);
                    });
            if (request.getEmail().equals(existingProfile.getEmail())) {
                log.info("ProfileService::updateEmail - New email is the same as current. No update needed. [userId: {}]", userId);
                return profileMapper.toDto(existingProfile);
            }
            try {
                UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
                UserRepresentation userRepresentation = userResource.toRepresentation();
                log.info("ProfileService::updateEmail - Updating email in Keycloak. [userId: {}, oldEmailKeycloak: {}, newEmail: {}]",
                        userId, userRepresentation.getEmail(), request.getEmail());
                userRepresentation.setEmail(request.getEmail());
                userRepresentation.setEmailVerified(false);
                userResource.update(userRepresentation);

            } catch (jakarta.ws.rs.NotFoundException e) {
                log.error("ProfileService::updateEmail - User not found in Keycloak. [userId: {}]", userId, e);
                throw new ProfileNotFoundException("User not found in Keycloak with ID: " + userId, e);
            } catch (Exception e) {
                log.error("ProfileService::updateEmail - Keycloak error during email update. [userId: {}]", userId, e);
                throw new ProfileServiceException("Keycloak error during email update: " + e.getMessage(), e);
            }

            existingProfile.setEmail(request.getEmail());
            Profile updatedProfile = profileRepository.save(existingProfile);
            log.info("ProfileService::updateEmail - Email (and possibly username) updated successfully in local DB. [userId: {}]", userId);
            log.info("ProfileService::updateEmail - Execution ended successfully. [userId: {}]", userId);
            return profileMapper.toDto(updatedProfile);
        } catch (ProfileServiceException | ProfileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("ProfileService::updateEmail - Failed to update email. [userId: {}]", userId, e);
            throw new ProfileServiceException("Failed to update email: " + e.getMessage(), e);
        }
    }

    @Transactional
    @PreAuthorize("#a0 == authentication.principal.subject or hasRole('ADMIN')")
    @CachePut(cacheNames = "cacheProfileById", key = "#result.userId")
    public ProfileResponse updateProfile(String userId, ProfileUpdateRequest request, MultipartFile profileImage, MultipartFile bannerImage) {
        log.info("ProfileService::updateProfile - Execution started. [userId: {}]", userId);
        try {
            Profile existingProfile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warn("ProfileService::updateProfile - Profile not found in local DB. [userId: {}]", userId);
                        return new ProfileNotFoundException("Not found profile with user-id: " + userId);
                    });

            String finalProfileImgUrl = existingProfile.getProfileImageUrl();
            if (profileImage != null && !profileImage.isEmpty()) {
                log.info("ProfileService::updateProfile - Attempting to upload profile image. [userId: {}]", userId);
                try {
                    MultipartFile[] profileImages = new MultipartFile[]{profileImage};
                    FeignApiResponse<List<MediaResponse>> mediaResponse = mediaClient.uploadFiles(profileImages, "IMAGE");
                    if (mediaResponse != null) {
                        finalProfileImgUrl = mediaResponse.getResult().getFirst().getMediaUrl();
                        log.info("ProfileService::updateProfile - Profile image updated successfully. [userId: {}, newUrl: {}]", userId, finalProfileImgUrl);
                    } else {
                        log.warn("ProfileService::updateProfile - Profile image upload returned no URL. [userId: {}]", userId);
                    }
                } catch (Exception e) {
                    log.warn("ProfileService::updateProfile - Failed to upload profile image. [userId: {}]", userId, e);
                }
            }
            String finalBannerImageUrl = existingProfile.getBannerImageUrl();
            if (bannerImage != null && !bannerImage.isEmpty()) {
                log.info("ProfileService::updateProfile - Attempting to upload banner image. [userId: {}]", userId);
                try {
                    MultipartFile[] bannerImages = new MultipartFile[]{bannerImage};
                    FeignApiResponse<List<MediaResponse>> mediaResponse = mediaClient.uploadFiles(bannerImages, "IMAGE");
                    if (mediaResponse != null) {
                        finalBannerImageUrl = mediaResponse.getResult().getFirst().getMediaUrl();
                        log.info("ProfileService::updateProfile - Banner image updated successfully. [userId: {}, newUrl: {}]", userId, finalBannerImageUrl);
                    } else {
                        log.warn("ProfileService::updateProfile - Banner image upload returned no URL. [userId: {}]", userId);
                    }
                } catch (Exception e) {
                    log.warn("ProfileService::updateProfile - Failed to upload banner image. [userId: {}]", userId, e);
                }
            }
            existingProfile.setDisplayName(request.getDisplayName());
            existingProfile.setBio(request.getBio());
            existingProfile.setLocation(request.getLocation());
            existingProfile.setWebsiteUrl(request.getWebsiteUrl());
            existingProfile.setProfileImageUrl(finalProfileImgUrl);
            existingProfile.setBannerImageUrl(finalBannerImageUrl);
            existingProfile.setDateOfBirth(request.getDateOfBirth());

            Profile updatedProfile = profileRepository.save(existingProfile);
            log.info("ProfileService::updateProfile - Profile updated successfully in local DB. [userId: {}]", userId);
            log.info("ProfileService::updateProfile - Execution ended successfully. [userId: {}]", userId);
            return profileMapper.toDto(updatedProfile);
        } catch (ProfileServiceException | ProfileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("ProfileService::updateProfile - Failed to update profile. [userId: {}]", userId, e);
            throw new ProfileServiceException("Failed to update profile: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("#a0 == authentication.principal.subject or hasRole('ADMIN')")
    public void changePassword(String userId, ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String username = jwt.getClaim("preferred_username");
        log.info("ProfileService::changePassword - Execution started. [userId: {}]", userId);

        if (!request.isValid()) {
            log.error("ProfileService::changePassword - Passwords do not match. [userId: {}]", userId);
            throw new ProfileServiceException("New password and confirm password do not match");
        }

        if (!isValidOldPassword(username, request.getOldPassword())) {
            log.error("ProfileService::changePassword - Old password is invalid. [username: {}]", username);
            throw new ProfileServiceException("Incorrect old password");
        }

        try {
            log.info("ProfileService::changePassword - Changing password in Keycloak. [userId: {}]", userId);
            UserResource userResource = keycloakAdminClient.realm(realm).users().get(userId);
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(request.getNewPassword());
            credential.setTemporary(false);
            userResource.resetPassword(credential);
            log.info("ProfileService::changePassword - Password changed successfully in Keycloak. [userId: {}]", userId);
        } catch (jakarta.ws.rs.NotFoundException e) {
            log.error("ProfileService::changePassword - User not found in Keycloak. [userId: {}]", userId, e);
            throw new ProfileNotFoundException("User not found in Keycloak with ID: " + userId, e);
        } catch (Exception e) {
            log.error("ProfileService::changePassword - Keycloak error during password change. [userId: {}]", userId, e);
            throw new ProfileServiceException("Keycloak error during password change: " + e.getMessage(), e);
        }
    }

    @PreAuthorize("#a0 == authentication.principal.subject or hasRole('ADMIN')")
    public void deleteProfile(String userId) {
        log.info("ProfileService::deleteProfile - Execution started. [userId: {}]", userId);
        try {
            Profile existingProfile = profileRepository.findByUserId(userId)
                    .orElseThrow(() -> {
                        log.warn("ProfileService::deleteProfile - Profile not found in local DB. [userId: {}]", userId);
                        return new ProfileNotFoundException("Not found profile with user-id: " + userId);
                    });
            String oldUsername = existingProfile.getUsername();
            try {
                log.info("ProfileService::deleteProfile - Deleting user from Keycloak. [userId: {}]", userId);
                keycloakAdminClient.realm(realm).users().delete(userId);
                log.info("ProfileService::deleteProfile - User deleted successfully from Keycloak. [userId: {}]", userId);
            } catch (jakarta.ws.rs.NotFoundException e) {
                log.error("ProfileService::deleteProfile - User not found in Keycloak. [userId: {}]", userId, e);
                throw new ProfileNotFoundException("User not found in Keycloak with ID: " + userId, e);
            } catch (Exception e) {
                log.error("ProfileService::deleteProfile - Keycloak error during user deletion. [userId: {}]", userId, e);
                throw new ProfileServiceException("Keycloak error during user deletion: " + e.getMessage(), e);
            }

            profileRepository.delete(existingProfile);
            cacheManager.getCache("cacheProfileByUsername").evict(oldUsername);
            cacheManager.getCache("cacheProfileById").evict(userId);
            log.info("ProfileService::deleteProfile - Profile deleted successfully from local DB. [userId: {}]", userId);
        } catch (ProfileServiceException | ProfileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("ProfileService::deleteProfile - Failed to delete profile. [userId: {}]", userId, e);
            throw new ProfileServiceException("Failed to delete profile: " + e.getMessage(), e);
        }
    }

    private boolean isValidOldPassword(String username, String password) {
        try (Keycloak keycloak = KeycloakBuilder.builder()
                .grantType(OAuth2Constants.PASSWORD)
                .serverUrl(serverUrl)
                .realm(realm)
                .username(username)
                .password(password)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .scope("openid")
                .build()) {
            keycloak.tokenManager().getAccessToken();
            return true;
        } catch (Exception e) {
            log.error("ProfileService::isValidOldPassword - Invalid old password. [username: {}]", username, e);
            return false;
        }
    }
}
