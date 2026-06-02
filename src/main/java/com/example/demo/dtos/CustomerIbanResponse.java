package com.example.demo.dtos;

import java.util.List;

public record CustomerIbanResponse(
        Long userId,
        String firstName,
        String lastName,
        List<String> ibans
) {
}

