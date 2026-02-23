package com.example.salon.exception;

public class BusinessNotActiveException extends RuntimeException {
    public BusinessNotActiveException(String message) {
        super(message);
    }
}