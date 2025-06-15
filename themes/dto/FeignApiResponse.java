package com.david.common.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeignApiResponse<T> {

    private int status;

    private String message;

    private T result;
}
