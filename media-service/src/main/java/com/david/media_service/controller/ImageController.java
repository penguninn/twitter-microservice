package com.david.media_service.controller;

import com.david.media_service.dto.response.ApiResponse;
import com.david.media_service.service.ImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/images")
public class ImageController {

    private final ImageProcessingService imageProcessingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse.Payload<String>> uploadImage(@RequestParam("file") MultipartFile file) {
        log.info("MediaController::uploadImage execution started");
        String result = imageProcessingService.uploadImage(file);
        log.info("MediaController::uploadImage execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Upload image successfully", result);
    }

}
