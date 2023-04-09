package com.javaexample.java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class HttpClientConfig {
    @Bean
    WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
