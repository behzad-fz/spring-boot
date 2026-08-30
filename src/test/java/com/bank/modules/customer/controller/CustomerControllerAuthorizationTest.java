package com.bank.modules.customer.controller;

import com.bank.modules.customer.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CustomerControllerAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    @Test
    void searchWithoutAuthIsForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/customers/search").param("queryString", "x"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void searchAllowedForUser() throws Exception {
        when(customerService.searchForCustomer(org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/v1/customers/search").param("queryString", "x"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "CUSTOMER")
    void searchDeniedWithoutUserAuthority() throws Exception {
        mockMvc.perform(get("/api/v1/customers/search").param("queryString", "x"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateWithoutAuthIsForbidden() throws Exception {
        mockMvc.perform(put("/api/v1/customers/{uuid}", "abc"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteWithoutAuthIsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/customers/{uuid}", "abc"))
                .andExpect(status().isForbidden());
    }
}
