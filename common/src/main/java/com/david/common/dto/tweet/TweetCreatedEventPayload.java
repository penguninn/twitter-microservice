package com.david.common.dto.tweet;

import com.david.common.dto.media.MediaResponse;
import com.david.common.enums.Visibility;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TweetCreatedEventPayload implements Serializable {

    private String tweetId;

    private String userId;

    private long createdAt;
}
