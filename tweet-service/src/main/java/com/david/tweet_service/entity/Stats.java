package com.david.tweet_service.entity;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Stats {

    @Builder.Default
    private long likesCount = 0;
}
