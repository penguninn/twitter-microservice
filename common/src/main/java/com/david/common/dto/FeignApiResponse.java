package com.david.common.dto;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class FeignApiResponse<T> implements Serializable {

    private int status;

    private String message;

    private T result;
}
