package com.david.profile_service.repository;

import com.david.profile_service.dto.response.ApiResponse;
import com.david.profile_service.dto.response.FeignApiResponse;
import com.david.profile_service.dto.response.MediaResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@FeignClient(name = "media-service", url = "${app.services.media-service.url}")
public interface MediaClient {

    @PostMapping(value = "/api/v1/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    FeignApiResponse<List<MediaResponse>> uploadFiles(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("type") String type
    );
}
