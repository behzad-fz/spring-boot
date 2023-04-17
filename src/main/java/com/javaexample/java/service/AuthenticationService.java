package com.javaexample.java.service;

import com.javaexample.java.controller.auth.AuthenticationRequest;
import com.javaexample.java.controller.auth.AuthenticationResponse;
import com.javaexample.java.controller.auth.RegisterRequest;
import com.javaexample.java.entity.Role;
import com.javaexample.java.entity.User;
import com.javaexample.java.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;

    private final TokenService tokenService;

    private final PasswordEncoder encoder;

    private final AuthenticationManager manager;
    public AuthenticationResponse register(RegisterRequest request) {
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .username(request.getUsername())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

    var jwtToken = tokenService.generateToken(user);

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

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
