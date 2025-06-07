package com.david.tweet_service.exception;

public class TweetServiceException extends RuntimeException {

    public TweetServiceException(String message) {
        super(message);
    }

    public TweetServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TweetServiceException(Throwable cause) {
        super(cause);
    }
}
