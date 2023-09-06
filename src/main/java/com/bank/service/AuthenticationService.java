package com.bank.service;

import com.bank.controller.auth.AuthenticationRequest;
import com.bank.controller.auth.AuthenticationResponse;
import com.bank.controller.auth.RegisterRequest;
import com.bank.entity.Role;
import com.bank.entity.Token;
import com.bank.entity.TokenType;
import com.bank.entity.User;
import com.bank.repository.TokenRepository;
import com.bank.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    private final UserRepository userRepository;

    private final TokenRepository tokenRepository;

    private final TokenService tokenService;

    private final PasswordEncoder encoder;

    private final AuthenticationManager manager;

    public AuthenticationService(
            UserRepository userRepository,
            TokenRepository tokenRepository,
            TokenService tokenService,
            PasswordEncoder encoder,
            @Qualifier("customAuthenticationManager") AuthenticationManager manager
    ) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.manager = manager;
    }

    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        var savedUser = userRepository.save(user);

        var jwtToken = tokenService.generateToken(user);

        saveToken(savedUser, jwtToken);

        return AuthenticationResponse.builder()
            .token(jwtToken)
            .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        manager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        var user = userRepository.findByUsername(request.getUsername()).orElseThrow();

        var jwtToken = tokenService.generateToken(user);

        revokeAllTokens(user);
        saveToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }

    private void saveToken(User user, String jwtToken) {
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeAllTokens(User user) {
        var validTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validTokens.isEmpty())
            return;

        validTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });

        tokenRepository.saveAll(validTokens);
    }
}
