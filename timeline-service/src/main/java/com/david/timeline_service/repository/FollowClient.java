package com.david.timeline_service.repository;

import com.david.common.dto.FeignApiResponse;
import com.david.common.dto.PageResponse;
import com.david.common.dto.follow.FollowResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "follow-service", url = "${app.services.follow-service.url}")
public interface FollowClient {

    @GetMapping("/api/v1/follows/{userId}/followers")
    FeignApiResponse<PageResponse<List<FollowResponse>>> getFollowers(
            @PathVariable("userId") String userId,
            @RequestParam(name = "page") int page,
            @RequestParam(name = "size") int size,
            @RequestParam(name = "sortBy") String sortBy
    );

}
