package com.bank.modules.transaction.controller;

import com.bank.modules.transaction.entity.Recipient;
import com.bank.modules.transaction.request.NewRecipient;
import com.bank.modules.transaction.service.RecipientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customers/{customerUUID}/recipients")
public class RecipientController {

    private final RecipientService recipientService;

    public RecipientController(RecipientService recipientService) {
        this.recipientService = recipientService;
    }

    @PostMapping
    private ResponseEntity<Recipient> save(
        @Valid @RequestBody NewRecipient request,
        @PathVariable String customerUUID
    ) {
        Recipient recipient = recipientService.save(request, customerUUID);

        return ResponseEntity.ok(recipient);
    }
}
