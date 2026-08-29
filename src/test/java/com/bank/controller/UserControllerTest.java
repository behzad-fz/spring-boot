package com.bank.controller;

import com.bank.entity.User;
import com.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

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
    void signUpEncodesPasswordAndPersists() throws Exception {
        when(userService.saveUser(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String body = objectMapper.writeValueAsString(Map.of(
                "username", "alice",
                "password", "plain",
                "role", "USER"
        ));

        mockMvc.perform(post("/users/sign-up")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        verify(userService).saveUser(any(User.class));
    }

    @Test
    void guestWithoutAuthIsRejected() throws Exception {
        mockMvc.perform(get("/users/guest"))
                .andExpect(status().isForbidden());
    }
}
