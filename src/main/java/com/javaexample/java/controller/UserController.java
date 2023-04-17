package com.javaexample.java.controller;

import com.javaexample.java.entity.User;
import com.javaexample.java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

        @Autowired
    private PasswordEncoder encoder;

    @GetMapping("/{id}")
    public String findUserById(@PathVariable("id") Long id, Principal principal) {
        return "find user "+ id + principal.getName();
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
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
