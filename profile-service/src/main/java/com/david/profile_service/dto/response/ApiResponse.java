package com.david.profile_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

public class ApiResponse <T> extends ResponseEntity<ApiResponse.Payload<T>> {

    public ApiResponse(HttpStatusCode status, String message) {
        super(new Payload<>(status.value(), message), status);
    }

    public ApiResponse(HttpStatusCode status, String message, T result) {
        super(new Payload<>(status.value(), message, result), status);
    }

    @Getter
    public static class Payload<T> {
        private final int status;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private final String message;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private T result;

        public Payload(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public Payload(int status, String message, T data) {
            this.status = status;
            this.message = message;
            this.result = data;
        }

    }
}