package com.bank.modules.customer.controller.auth;


import com.bank.controller.auth.AuthenticationRequest;
import com.bank.controller.auth.AuthenticationResponse;
import com.bank.modules.customer.service.CustomerAuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/customer-auth")
@RequiredArgsConstructor
public class CustomerAuthController {

    private final CustomerAuthenticationService authenticationService;

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }
}
