package com.bank.modules.customer.controller.auth;

import com.bank.modules.customer.repository.CustomerRepository;
import com.bank.modules.customer.repository.CustomerTokenRepository;
import com.bank.modules.customer.service.CustomerAuthenticationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerAuthControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerAuthenticationService authenticationService;

    @MockBean
    private CustomerRepository customerRepository;

    @MockBean
    private CustomerTokenRepository customerTokenRepository;

    @MockBean
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    private static final String BODY = "{\"username\":\"user1\",\"password\":\"pass1\"}";

    @Test
    void updateMyCredentialsRejectsAnonymous() throws Exception {
        mockMvc.perform(put("/api/v1/customer-auth/update-my-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void updateMyCredentialsRejectsNonCustomerPrincipal() throws Exception {
        mockMvc.perform(put("/api/v1/customer-auth/update-my-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateMyCredentialsRejectsInvalidBody() throws Exception {
        mockMvc.perform(put("/api/v1/customer-auth/update-my-credentials")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\",\"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }
}
