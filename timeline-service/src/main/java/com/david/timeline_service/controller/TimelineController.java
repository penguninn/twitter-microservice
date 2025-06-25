package com.david.timeline_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.timeline_service.service.TimelineService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/api/v1/timeline")
    public ApiResponse<?> getTimeline(
            @RequestParam(name = "page", defaultValue = "1") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "sortBy", defaultValue = "createdAt,desc") String sortBy) {
        log.info("TimelineController::getTimeline - Fetching timeline");
        var response = timelineService.getTimeline(page, size, sortBy);
        log.info("TimelineController::getTimeline - Timeline fetched successfully");
        return new ApiResponse<>(HttpStatus.OK, "Timeline fetched successfully", response);
    }
}
