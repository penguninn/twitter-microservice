package com.david.profile_service.controller;

import com.david.profile_service.dto.request.ProfileRegisterRequest;
import com.david.profile_service.dto.response.ApiResponse;
import com.david.profile_service.dto.response.ProfileResponse;
import com.david.profile_service.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping("/register")
    public ApiResponse<?> register(@Valid @RequestBody ProfileRegisterRequest request) {
        log.info("ProfileController::register execution started");
        ProfileResponse profileResponse = profileService.register(request);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.CREATED, "Register successfully", profileResponse);
        log.info("ProfileController::register execution end");
        return response;
    }

    @GetMapping("/{username}")
    public ApiResponse<?> getProfile(@Valid @PathVariable(name = "username") String username) {
        log.info("ProfileController::getProfile execution started");
        ProfileResponse profileResponse = profileService.getProfile(username);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.CREATED, "Get profile successfully", profileResponse);
        log.info("ProfileController::getProfile execution ended");
        return response;
    }

    @GetMapping("/profiles")
    public ApiResponse<?> getAllProfile(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "firstName,asc") String sortBy
    ) {
        log.info("ProfileController::getAllProfile execution started");
        List<ProfileResponse> profileResponses = profileService.getAllProfile(page, size, sortBy);
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.OK, "Get all profile successfully", profileResponses);
        log.info("ProfileController::getAllProfile execution ended");
        return response;
    }

}
