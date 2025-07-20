package com.david.api_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @RequestMapping(value = "/profile-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String userFallback() {
        return "Profile service is not available";
    }

    @RequestMapping(value = "/media-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String mediaFallback() {
        return "Media service is not available";
    }

    @RequestMapping(value = "/tweet-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String tweetFallback() {
        return "Tweet service is not available";
    }

    @RequestMapping(value = "/follow-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String followFallback() {
        return "Follow service is not available";
    }

    @RequestMapping(value = "/timeline-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String timelineFallback() {
        return "Timeline service is not available";
    }

    @RequestMapping(value = "/comment-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String commentFallback() {
        return "Comment service is not available";
    }

    @RequestMapping(value = "/notification-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String notificationFallback() {
        return "Notification service is not available";
    }

    @RequestMapping(value = "/search-fallback", method = {RequestMethod.GET, RequestMethod.POST})
    public String searchFallback() {
        return "Search service is not available";
    }
}
