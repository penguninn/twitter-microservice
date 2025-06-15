package com.david.comment_service.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CommentRequest implements Serializable {

    @NotNull(message = "Content cannot be null")
    private String content;
}
