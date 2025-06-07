package com.david.tweet_service.exception;

public class TweetNotFoundException extends RuntimeException {

    public TweetNotFoundException(String message) {
        super(message);
    }

    public TweetNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
