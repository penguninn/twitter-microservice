package com.david.follow_service.exception;

public class NotFollowingException extends RuntimeException {
  public NotFollowingException(String followerId, String followedId) {
    super(String.format("User %s is not following user %s", followerId, followedId));
  }
}
