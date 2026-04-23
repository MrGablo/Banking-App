package com.example.demo.controllers;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @CrossOrigin(origins = "${app.cors.allowed-origin:http://localhost:5173}")
    @GetMapping("/test")
    public String testEndpoint() {
        return "Hello from backend !";
    }
}

