package com.example.demo.services;

/**
 * Deprecated wrapper to ease transition while JwtService was moved to
 * com.example.demo.util.JwtService. This class will fail fast if used.
 */
@Deprecated
public class JwtService {

    public JwtService() {
        throw new IllegalStateException("JwtService moved to package com.example.demo.util.JwtService; use that class instead.");
    }
}
