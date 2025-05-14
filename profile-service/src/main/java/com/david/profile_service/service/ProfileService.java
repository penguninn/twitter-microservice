package com.david.profile_service.service;

import com.david.profile_service.dto.request.Credential;
import com.david.profile_service.dto.request.ProfileRegisterRequest;
import com.david.profile_service.dto.request.TokenExchangeRequest;
import com.david.profile_service.dto.request.UserCreationRequest;
import com.david.profile_service.dto.response.ProfileResponse;
import com.david.profile_service.dto.response.TokenExchangeResponse;
import com.david.profile_service.entity.Profile;
import com.david.profile_service.exception.ProfileNotFoundException;
import com.david.profile_service.exception.ProfileServiceException;
import com.david.profile_service.mapper.ProfileMapper;
import com.david.profile_service.repository.IdentityProvider;
import com.david.profile_service.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final IdentityProvider identityProvider;

    @Value("${idp.client-id}")
    private String clientId;

    @Value("${idp.client-secret}")
    private String clientSecret;

    public ProfileResponse register(ProfileRegisterRequest request) {
        try {
            log.info("ProfileService::register execution started");
            TokenExchangeResponse tokenInfo = identityProvider.exchangeToken(TokenExchangeRequest.builder()
                    .grant_type("client_credentials")
                    .client_id(clientId)
                    .client_secret(clientSecret)
                    .scope("openid")
                    .build());
            log.info("AccessToken {}", tokenInfo.getAccessToken());
            ResponseEntity<?> creationResponse = identityProvider.createUser(
                    "Bearer " + tokenInfo.getAccessToken(),
                    UserCreationRequest.builder()
                            .username(request.getUsername())
                            .enabled(true)
                            .email(request.getEmail())
                            .firstName(request.getFirstName())
                            .lastName(request.getLastName())
                            .emailVerified(false)
                            .credentials(List.of(
                                    Credential.builder()
                                            .type("password")
                                            .temporary(false)
                                            .value(request.getPassword())
                                            .build()
                            ))
                            .build());
            String userId = extractUserId(creationResponse);
            Profile profile = ProfileMapper.mapToEntity(request);
            profile.setUserId(userId);
            log.info("ProfileService::register execution ended");
            return ProfileMapper.mapToDto(profileRepository.save(profile));
        } catch (Exception e) {
            log.error("Failed to persist user: {}", e.getMessage());
            throw new ProfileServiceException("Failed to persist user: " + e.getMessage());
        }
    }

    public ProfileResponse getProfile(String username) {
        log.info("ProfileService::getProfile execution started");
        Profile profile = profileRepository.findByUsername(username)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
        log.info("ProfileService::getProfile execution ended");
        return ProfileMapper.mapToDto(profile);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ProfileResponse> getAllProfile(int page, int size, String sortBy) {
        try {
            log.info("ProfileService::getAllProfile execution started");
            int p = page > 0 ? page - 1 : 0;
            String[] sortParams = sortBy.split(",");
            Sort sortOrder = Sort.by(Sort.Direction.fromString(sortParams[1]), sortParams[0]);
            Pageable pageable = PageRequest.of(p, size, sortOrder);
            Page<Profile> profiles = profileRepository.findAll(pageable);
            List<ProfileResponse> profileResponses = profiles.stream()
                    .map(profile -> ProfileResponse.builder()
                            .userId(profile.getUserId())
                            .username(profile.getUsername())
                            .email(profile.getEmail())
                            .firstName(profile.getFirstName())
                            .lastName(profile.getLastName())
                            .dob(profile.getDob())
                            .build())
                    .toList();
            log.info("ProfileService::getAllProfile execution ended");
            return profileResponses;
        } catch (RuntimeException e) {
            throw new ProfileServiceException("Failed to get all profiles: " + e.getMessage());
        }

    }

    public String extractUserId(ResponseEntity<?> response) {
        try {
            String location = response.getHeaders().getFirst("Location");
            if (location == null || !location.contains("/")) {
                throw new ProfileServiceException("Invalid user creation response, missing Location header");
            }
            String[] parts = location.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            throw new ProfileServiceException("Failed to extract user ID from response: " + e);
        }
    }
}
