package com.david.search_service.controller;

import com.david.common.dto.ApiResponse;
import com.david.search_service.dto.response.TweetSearchResponseDTO;
import com.david.search_service.dto.response.UserSearchResponseDTO;
import com.david.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/tweets")
    public ResponseEntity<ApiResponse<Page<TweetSearchResponseDTO>>> searchTweets(
            @RequestParam("q") String query,
            @PageableDefault(size = 10) Pageable pageable) {
        String currentUserId = getCurrentUserId();
        Page<TweetSearchResponseDTO> results = searchService.searchTweets(query, currentUserId, pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Search tweets successfully", results));
    }

    @GetMapping("/users")
    public ResponseEntity<ApiResponse<Page<UserSearchResponseDTO>>> searchUsers(
            @RequestParam("q") String query,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<UserSearchResponseDTO> results = searchService.searchUsers(query, pageable);
        return ResponseEntity.ok(new ApiResponse<>(HttpStatus.OK, "Search users successfully", results));
    }

    private String getCurrentUserId() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
//            return jwt.getSubject();
//        }
        return "anonymous";
    }
}