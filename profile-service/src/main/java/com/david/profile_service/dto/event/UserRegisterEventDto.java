package com.david.profile_service.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterEventDto {
    private String userId;
    private String username;
    private String email;
    private String displayName;
    private String profileImgUrl;
    private String eventType;
}
