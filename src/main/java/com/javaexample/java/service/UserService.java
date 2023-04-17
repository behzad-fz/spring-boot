package com.javaexample.java.service;

import com.javaexample.java.entity.User;
import com.javaexample.java.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

//    @Autowired
//    private PasswordEncoder encoder;

    public User saveUser(User user) {
        return userRepository.save(user);
    }
}
