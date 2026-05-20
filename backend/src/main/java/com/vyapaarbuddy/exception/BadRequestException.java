package com.vyapaarbuddy.exception;

/**
 * Exception thrown for bad request scenarios.
 * TODO: Add error code support
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
