package com.david.profile_service.dto.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenExchangeRequest {
    private String grant_type;
    private String client_id;
    private String client_secret;
    private String scope;
}
