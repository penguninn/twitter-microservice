package com.david.profile_service.repository;

import com.david.profile_service.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@FeignClient(name = "media-service", url = "${client.media-service.url}")
public interface MediaClient {

    @PostMapping(value = "/api/v1/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ResponseEntity<ApiResponse.Payload<String>> uploadImage(@RequestPart("file") MultipartFile file);

}
