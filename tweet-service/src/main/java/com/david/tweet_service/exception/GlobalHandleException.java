package com.david.tweet_service.exception;

import com.david.tweet_service.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.add(error.getDefaultMessage()));
        ex.getBindingResult().getGlobalErrors()
                .forEach(error -> errors.add(error.getDefaultMessage()));
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Invalid validation", errors);
    }

    @ExceptionHandler(TweetNotFoundException.class)
    public ApiResponse<?> handleTweetNotFoundException(TweetNotFoundException ex) {
        return new ApiResponse<>(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TweetServiceException.class)
    public ApiResponse<?> handleTweetServiceExceptions(TweetServiceException ex) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

}
