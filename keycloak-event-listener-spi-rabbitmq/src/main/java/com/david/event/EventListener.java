package com.david.event;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventListener<T> implements Serializable {

    private String eventId;
    private String eventType;
    private long timestamp;

    private T payload;
}
