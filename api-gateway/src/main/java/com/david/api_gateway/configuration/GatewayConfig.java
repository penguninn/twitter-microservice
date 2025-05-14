package com.david.api_gateway.configuration;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("profile-service", r -> r.path("/profile/**")
                        .filters(f -> f
//                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("user-circuit-breaker")
                                        .setFallbackUri("forward:/profile-fallback")
                                )
                        )
                        .uri("lb://profile-service"))
                .build();
    }
}
