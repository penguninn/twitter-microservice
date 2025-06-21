package com.david.common.dto;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiEventMessage<T> implements Serializable {

    private String eventId;

    private String eventType;

    private String timestamp;

    private T payload;
}
