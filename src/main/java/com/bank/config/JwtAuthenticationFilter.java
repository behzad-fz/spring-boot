package com.bank.config;

import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.repository.TokenRepository;
import com.bank.repository.UserRepository;
import com.bank.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.security.InvalidParameterException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final TokenRepository tokenRepository;
    private final CustomerTokenRepository customerTokenRepository;
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;
        final String userType;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // why 7 ? because "Bearer " length is 7 and token starts after that index
        jwt = authHeader.substring(7);
        username = tokenService.extractUserName(jwt);
        userType = tokenService.extractUserType(jwt);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService(userType).loadUserByUsername(username);

            if (tokenService.isTokenValid(jwt, userDetails) && isJwtValid(userType, jwt)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetailsService userDetailsService(String userType) {
        return switch (userType) {
            case "user" -> new CustomUserDetailsService(userRepository);
            case "customer" -> new CustomCustomerDetailsService(customerRepository);
            default -> throw new InvalidParameterException("User Type not supported");
        };
    }

    private boolean isJwtValid(String userType, String jwt) {
        return switch (userType) {
            case "user" -> tokenRepository.findByToken(jwt).map(token -> !token.isExpired() && !token.isRevoked())
                    .orElse(false);
            case "customer" -> customerTokenRepository.findByToken(jwt).map(token -> !token.isExpired() && !token.isRevoked())
                    .orElse(false);
            default -> throw new InvalidParameterException("User Type not supported");
        };
    }
}
