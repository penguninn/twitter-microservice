package com.david.profile_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProfileCreationRequest implements Serializable {

    private String userId;

    private String username;

    private String email;
}
