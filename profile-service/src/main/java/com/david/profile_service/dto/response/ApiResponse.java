package com.david.profile_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

public class ApiResponse <T> extends ResponseEntity<ApiResponse.Payload<T>> implements Serializable {

    public ApiResponse(HttpStatusCode status, String message) {
        super(new Payload<>(status.value(), message), status);
    }

    public ApiResponse(HttpStatusCode status, String message, T result) {
        super(new Payload<>(status.value(), message, result), status);
    }

    @Getter
    @Setter
    public static class Payload<T> {
        private int status;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private String message;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        private T result;

        public Payload() {
        }

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