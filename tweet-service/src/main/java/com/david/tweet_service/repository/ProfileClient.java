package com.david.tweet_service.repository;

import com.david.tweet_service.dto.response.FeignApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "profile-service", url = "${app.services.profile-service.url}")
public interface ProfileClient {

    @GetMapping("/api/v1/profiles/i/{userId}")
    FeignApiResponse<Boolean> getProfileById(@PathVariable("userId") String userId);

}
