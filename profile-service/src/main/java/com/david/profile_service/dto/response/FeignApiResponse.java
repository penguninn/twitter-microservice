package com.david.profile_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeignApiResponse<T> {

    private int status;

    private String message;

    private T result;
}
