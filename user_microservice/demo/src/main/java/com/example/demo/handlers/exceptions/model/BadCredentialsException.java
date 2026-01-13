package com.example.demo.handlers.exceptions.model;


public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}