package com.david.timeline_service.exception;

public class TimelineServiceException extends RuntimeException {

    public TimelineServiceException(String message) {
        super(message);
    }

    public TimelineServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public TimelineServiceException(Throwable cause) {
        super(cause);
    }
}
