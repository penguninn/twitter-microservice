package com.david.mediaservice.exception;

import com.david.mediaservice.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@ControllerAdvice
public class GlobalHandleException {

    @ExceptionHandler(AzureStorageServiceException.class)
    public ApiResponse<?> handleAzureStorageServiceExceptions(AzureStorageServiceException ex) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(ImageProcessingServiceException.class)
    public ApiResponse<?> handleImageServiceExceptions(ImageProcessingServiceException ex) {
        return new ApiResponse<>(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ApiResponse<?> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex, WebRequest request) {
        return new ApiResponse<>(HttpStatus.PAYLOAD_TOO_LARGE, "File size exceeds the configured limit.");
    }
}
