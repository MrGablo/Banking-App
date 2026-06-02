package com.example.demo.controllers;

import com.example.demo.common.pagination.PageResponse;
import com.example.demo.dtos.ApproveCustomerRequest;
import com.example.demo.dtos.UserResponse;
import com.example.demo.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
public class UserController {

    private final UserService userService;

    @Value("${max.pagination.size}")
    private int maxPaginationSize;

    public UserController(UserService userService) {
      this.userService = userService;
    }

    @GetMapping("")
    public PageResponse<UserResponse> getCustomersWithoutAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(
                userService.getCustomersWithoutAccounts(PageRequest.of(page, Math.min(size, maxPaginationSize)))
        );
    }

    @PostMapping("/{userId}/approve")
    public ResponseEntity<UserResponse> approveCustomer(
            @PathVariable Long userId,
            @Valid @RequestBody ApproveCustomerRequest request) {
        UserResponse response = userService.approveCustomer(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
