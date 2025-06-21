package com.david.follow_service.exception;

public class SelfFollowException extends RuntimeException {
    public SelfFollowException() {
        super("User cannot follow themselves");
    }
}
