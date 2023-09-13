package com.bank.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

@Configuration
//@RequiredArgsConstructor
public class ApplicationConfig {
    private final Environment env;
    private final RsaKeyProperties rsaKeys;

    // @RequiredArgsConstructor annotation those the construction automatically
    public ApplicationConfig(Environment env, RsaKeyProperties rsaKeys) {
        this.env = env;
        this.rsaKeys = rsaKeys;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(rsaKeys.publicKey()).privateKey(rsaKeys.privateKey()).build();
        JWKSource<SecurityContext> jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(rsaKeys.publicKey()).build();
    }

    @Bean
    public CustomUserAuthenticationProvider customAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomUserAuthenticationProvider(userDetailsService,passwordEncoder);
    }

    @Bean
    public CustomCustomerAuthenticationProvider customCustomerAuthenticationProvider(CustomCustomerDetailsService customerDetailsService, PasswordEncoder passwordEncoder) {
        return new CustomCustomerAuthenticationProvider(customerDetailsService, passwordEncoder);
    }

    @Bean(name = "customAuthenticationManager")
    @Primary
    public AuthenticationManager customAuthenticationManager(CustomUserAuthenticationProvider customUserAuthenticationProvider) {
        return new ProviderManager(customUserAuthenticationProvider);
    }

    @Bean(name = "customCustomerAuthenticationManager")
    public AuthenticationManager customCustomerAuthenticationManager(CustomCustomerAuthenticationProvider customCustomerAuthProvider) {
        return new ProviderManager(customCustomerAuthProvider);
    }
}
