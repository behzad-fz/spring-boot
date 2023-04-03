package com.javaexample.java.controller;

import com.javaexample.java.entity.User;
import com.javaexample.java.service.TokenService;
import com.javaexample.java.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RestController
public class AuthController {
    private static final Logger LOG = LoggerFactory.getLogger(AuthController.class);

    private final TokenService tokenService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private JdbcUserDetailsManager jdbcUserDetailsManager;

    public AuthController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @PostMapping("/token")
    public String token(Authentication authentication) {
        LOG.debug("token requested for user '{}'", authentication.getName());
        String token = tokenService.generateToken(authentication);
        LOG.debug("generated token '{}'", token);

        return token;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        String plainPassword = user.getPassword();
        String encodedPassword = encoder.encode(plainPassword);
        user.setPassword(encodedPassword);

        UserDetails usr = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .roles("ORDINARY")
                .build();

        jdbcUserDetailsManager.createUser(usr);

        return ResponseEntity.status(201).build();
    }

}
