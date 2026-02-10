package com.badmintonshop.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.badmintonshop.entity.Account;
import com.badmintonshop.payload.response.AccountResponse;
import com.badmintonshop.service.AccountService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RequestMapping("/api/account")
@RestController()
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CASHIER')")
public class AccountController {
    private final AccountService accountService;
    
    @GetMapping("")
    public ResponseEntity<AccountResponse> getCurrentUser(@AuthenticationPrincipal Account loggedInUser) {
        if (loggedInUser == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(accountService.getAccountById(loggedInUser.getId()));
    }
    
    // @PutMapping("")
    // public ResponseEntity<AccountResponse> putMethodName(@AuthenticationPrincipal Account loggedInUser
    //                             , @RequestBody @Valid AccountRequest request ) {
        
    //     return ResponseEntity.ok(accountService.updateAccount(loggedInUser.getId(), request));
    // }

}
