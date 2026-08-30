package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

        @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String admin() {
        return "Hello admin!";
    }

    @GetMapping("/guest")
    public String guest() {
        return "Hello guest!";
    }

        @PostMapping("/sign-up")
    public ResponseEntity<?> saveUser(@RequestBody User user) {
        String pass = user.getPassword();
        user.setPassword(encoder.encode(pass));

        userService.saveUser(user);
        return ResponseEntity.status(201).build();
    }

}
