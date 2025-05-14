package com.david.profile_service.configuration;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CustomAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt source) {
        Map<String, Object> realmAccess = source.getClaimAsMap("realm_access");
        Object roles = realmAccess.get("roles");
        if(roles instanceof List<?> strRole) {
            return ((List<String>) strRole)
                    .stream()
                    .map(r -> new SimpleGrantedAuthority(String.format("%s%s", "ROLE_", r)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
