package com.david.common.dto.tweet;

import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StatsResponse implements Serializable {

    private long likesCount;
}
