package com.david.profile_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.profile.ProfileResponse;
import com.david.profile_service.dto.request.ChangePasswordRequest;
import com.david.profile_service.dto.request.EmailUpdateRequest;
import com.david.profile_service.dto.request.ProfileUpdateRequest;
import com.david.profile_service.dto.request.UsernameUpdateRequest;
import com.david.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/api/v1/profiles")
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/exists/{userId}")
    public ApiResponse<Boolean> checkProfileExists(@Valid @PathVariable(name = "userId") String userId) {
        log.info("ProfileController::checkProfileExists execution started");
        boolean exists = profileService.checkProfileExists(userId);
        ApiResponse<Boolean> response = new ApiResponse<>(HttpStatus.OK, "Check profile existence successfully", exists);
        log.info("ProfileController::checkProfileExists execution ended");
        return response;
    }

    @GetMapping("/i/{userId}")
    public ApiResponse<?> getProfileById(@Valid @PathVariable(name = "userId") String userId) {
        log.info("ProfileController::getProfileById execution started");
        ProfileResponse profileResponse = profileService.getProfileById(userId);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Get profile successfully", profileResponse);
        log.info("ProfileController::getProfileById execution ended");
        return response;
    }

    @GetMapping("/u/{username}")
    public ApiResponse<?> getProfileUsername(@Valid @PathVariable(name = "username") String username) {
        log.info("ProfileController::getProfileByUsername execution started");
        ProfileResponse profileResponse = profileService.getProfileByUsername(username);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Get profile successfully", profileResponse);
        log.info("ProfileController::getProfileByUsername execution ended");
        return response;
    }

    @GetMapping
    public ApiResponse<?> getAllProfile(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "displayName,asc") String sortBy
    ) {
        log.info("ProfileController::getAllProfile execution started");
        PageResponse<?> profileResponses = profileService.getAllProfile(page, size, sortBy);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Get all profile successfully", profileResponses);
        log.info("ProfileController::getAllProfile execution ended");
        return response;
    }

    @PatchMapping("/{userId}/username")
    public ApiResponse<?> updateUsername(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UsernameUpdateRequest request
    ) {
        log.info("ProfileController::updateUsername execution started");
        ProfileResponse profileResponse = profileService.updateUsername(userId, request);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Update username successfully", profileResponse);
        log.info("ProfileController::updateUsername execution end");
        return response;
    }

    @PatchMapping("/{userId}/email")
    public ApiResponse<?> updateEmail(
            @PathVariable("userId") String userId,
            @Valid @RequestBody EmailUpdateRequest request
    ) {
        log.info("ProfileController::updateEmail execution started");
        ProfileResponse profileResponse = profileService.updateEmail(userId, request);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Update email successfully", profileResponse);
        log.info("ProfileController::updateEmail execution end");
        return response;
    }

    @PatchMapping("/{userId}/change-password")
    public ApiResponse<?> changePassword(
            @PathVariable("userId") String userId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        log.info("ProfileController::updatePassword execution started");
        profileService.changePassword(userId, request);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Update password successfully");
        log.info("ProfileController::updatePassword execution end");
        return response;
    }

    @PatchMapping("/{userId}")
    public ApiResponse<?> updateProfile(
            @PathVariable("userId") String userId,
            @Valid @RequestPart(name = "profileData") ProfileUpdateRequest request,
            @RequestPart(name = "profileImage", required = false) MultipartFile profileImage,
            @RequestPart(name = "bannerImage", required = false) MultipartFile bannerImage
    ) {
        log.info("ProfileController::updateProfile execution started");
        ProfileResponse profileResponse = profileService.updateProfile(userId, request, profileImage, bannerImage);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Update profile successfully", profileResponse);
        log.info("ProfileController::updateProfile execution end");
        return response;
    }

    @DeleteMapping("/{userId}")
    public ApiResponse<?> deleteProfile(@PathVariable("userId") String userId) {
        log.info("ProfileController::deleteProfile execution started");
        profileService.deleteProfile(userId);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Delete profile successfully");
        log.info("ProfileController::deleteProfile execution end");
        return response;
    }
}
