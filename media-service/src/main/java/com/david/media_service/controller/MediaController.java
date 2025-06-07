package com.david.media_service.controller;

import com.david.media_service.dto.response.ApiResponse;
import com.david.media_service.dto.response.MediaResponse;
import com.david.media_service.enums.FileTypeGroup;
import com.david.media_service.service.MediaService;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/media")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<List<MediaResponse>> uploadFiles(
            @RequestParam("files") @NotEmpty MultipartFile[] files,
            @RequestParam("type") FileTypeGroup type
    ) {
        log.info("MediaController::uploadFiles - Execution started");
        List<MediaResponse> result = mediaService.uploadFiles(files, type.getAllowedContentTypes());
        log.info("MediaController::uploadFiles - Execution ended");
        return new ApiResponse<>(HttpStatus.OK, "Upload files successfully", result);
    }

}
