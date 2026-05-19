package com.example.demo.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateException extends ApiException {
    public DuplicateException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
