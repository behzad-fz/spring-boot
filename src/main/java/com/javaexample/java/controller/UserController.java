package com.javaexample.java.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/users")
public class UserController {

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
}