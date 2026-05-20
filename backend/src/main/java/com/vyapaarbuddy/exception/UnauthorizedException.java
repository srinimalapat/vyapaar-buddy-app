package com.vyapaarbuddy.exception;

/**
 * Exception thrown for unauthorized access scenarios.
 * TODO: Add permission details
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
