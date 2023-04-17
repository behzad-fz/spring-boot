//package com.javaexample.java.controller;
//
//import com.javaexample.java.config.SecurityConfig;
//import com.javaexample.java.service.TokenService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.Import;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
//
//@WebMvcTest({UserControllerTest.class, AuthController.class})
//@Import({SecurityConfig.class, TokenService.class})
//class UserControllerTest {
//
//    @Autowired
//    MockMvc mvc;
//
//    @Test
//    void rootWhenUnAuthenticatedThen401() throws Exception {
//        this.mvc.perform(get("/")).andExpect(status().isUnauthorized());
//    }
//
//    @Test
//    void rootWhenAuthenticated() throws Exception {
//        MvcResult result = this.mvc.perform(post("/token").with(httpBasic("behzad-fz", "password")))
//                .andExpect(status().isOk()).andReturn();
//
//        String token = result.getResponse().getContentAsString();
//
//        this.mvc.perform(get("/").header("Authorization", "Bearer "+ token))
//                .andExpect(content().string("Hello JAVA World!"));
//
//    }
//
//    @Test
//    @WithMockUser
//    void rootIsOkayRegardlessOfAuthentication() throws Exception {
//        this.mvc.perform(get("/")).andExpect(status().isOk());
//    }
//
//}