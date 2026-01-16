
package com.example.salon.exception;

/**
 * Exception thrown when user tries to access/modify resources they don't own
 */
public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String message) {
        super(message);
    }
}