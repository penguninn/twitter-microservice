package com.david.follow_service.exception;

import com.david.common.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Invalid validation", errors);
    }

    @ExceptionHandler(FollowServiceException.class)
    public ApiResponse<?> handleFollowServiceException(FollowServiceException ex) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(SelfFollowException.class)
    public ApiResponse<?> handleSelfFollowException(SelfFollowException ex) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Cannot follow yourself");
    }

    @ExceptionHandler(AlreadyFollowingException.class)
    public ApiResponse<?> handleAlreadyFollowingException(AlreadyFollowingException ex) {
        return new ApiResponse<>(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotFollowingException.class)
    public ApiResponse<?> handleNotFollowingException(NotFollowingException ex) {
        return new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ApiResponse<?> handleAccessDeniedException(AccessDeniedException ex) {
        return new ApiResponse<>(HttpStatus.FORBIDDEN, "Access denied: " + ex.getMessage());
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ApiResponse<?> handleAuthenticationCredentialsNotFoundException(AuthenticationCredentialsNotFoundException ex) {
        return new ApiResponse<>(HttpStatus.UNAUTHORIZED, "Authentication credentials not found: " + ex.getMessage());
    }

}
