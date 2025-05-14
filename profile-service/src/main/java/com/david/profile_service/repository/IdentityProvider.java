package com.david.profile_service.repository;

import com.david.profile_service.dto.request.TokenExchangeRequest;
import com.david.profile_service.dto.request.UserCreationRequest;
import com.david.profile_service.dto.response.TokenExchangeResponse;
import feign.QueryMap;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "identity-provider", url = "${idp.url}")
public interface IdentityProvider {
    @PostMapping(value = "/realms/twitter/protocol/openid-connect/token",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    TokenExchangeResponse exchangeToken(@QueryMap TokenExchangeRequest request);

    @PostMapping(value = "/admin/realms/twitter/users",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<?> createUser(
            @RequestHeader("authorization") String token,
            @RequestBody UserCreationRequest request);
}
