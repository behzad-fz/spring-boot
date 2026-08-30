package com.bank.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void guestReturnsHello() throws Exception {
        mockMvc.perform(get("/users/guest"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello guest!"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminReturnsHello() throws Exception {
        mockMvc.perform(get("/users/admin"))
                .andExpect(status().isOk())
                .andExpect(content().string("Hello admin!"));
    }

    @Test
    @WithMockUser
    void signUpEndpointIsRemoved() throws Exception {
        mockMvc.perform(post("/users/sign-up"))
                .andExpect(status().isNotFound());
    }

    @Test
    void guestWithoutAuthIsRejected() throws Exception {
        mockMvc.perform(get("/users/guest"))
                .andExpect(status().isForbidden());
    }
}