package com.david.follow_service.exception;

public class AlreadyFollowingException extends RuntimeException {
    public AlreadyFollowingException(String followerId, String followedId) {
      super(String.format("User %s is already following user %s", followerId, followedId));
    }
}
