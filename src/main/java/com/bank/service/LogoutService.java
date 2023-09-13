package com.bank.service;

import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;
    private final CustomerTokenRepository customerTokenRepository;
    private final TokenService tokenService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userType;

        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }

        jwt = authHeader.substring(7);

        userType = tokenService.extractUserType(jwt);

        if (userType.equals("user")) {
            var storedToken = tokenRepository.findByToken(jwt)
                    .orElse(null);

            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                tokenRepository.save(storedToken);
                SecurityContextHolder.clearContext();
            }
        } else {
            var storedToken = customerTokenRepository.findByToken(jwt)
                    .orElse(null);

            if (storedToken != null) {
                storedToken.setExpired(true);
                storedToken.setRevoked(true);
                customerTokenRepository.save(storedToken);
                SecurityContextHolder.clearContext();
            }
        }
    }
}
