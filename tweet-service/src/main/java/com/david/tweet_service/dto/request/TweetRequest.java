package com.david.tweet_service.dto.request;

import com.david.tweet_service.enums.Visibility;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetRequest {

    private String content;

    private List<String> hashtags;

    @NotNull
    private Visibility visibility;
}
