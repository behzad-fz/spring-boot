package com.bank.service;

import com.bank.entity.JwtToken;
import com.bank.entity.Token;
import com.bank.modules.customer.entity.CustomerToken;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.repository.TokenRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import java.security.InvalidParameterException;

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

        var storedToken = getJwt(userType, jwt);

        if (storedToken != null) {
            storedToken.setExpired(true);
            storedToken.setRevoked(true);
            revokeJwt(userType, storedToken);
            SecurityContextHolder.clearContext();
        }
    }

    private JwtToken getJwt(String userType, String jwt) {
        return switch (userType) {
            case "user" -> tokenRepository.findByToken(jwt).orElse(null);
            case "customer" -> customerTokenRepository.findByToken(jwt).orElse(null);
            default -> throw new InvalidParameterException("User Type not supported");
        };
    }

    private void revokeJwt(String userType, JwtToken jwt) {
        switch (userType) {
            case "user" -> tokenRepository.save((Token) jwt);
            case "customer" -> customerTokenRepository.save((CustomerToken) jwt);
            default -> throw new InvalidParameterException("User Type not supported");
        }
    }
}
