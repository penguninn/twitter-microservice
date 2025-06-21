package com.david.tweet_service.repository;

import com.david.common.dto.FeignApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "${app.services.profile-service.url}")
public interface ProfileClient {

    @GetMapping(value = "/api/v1/profiles/i/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    FeignApiResponse<Boolean> userExistsById(@PathVariable("userId") String userId);

}
