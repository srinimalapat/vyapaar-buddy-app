package com.vyapaarbuddy.exception;

/**
 * Exception thrown when a requested resource is not found.
 * TODO: Add resource type and ID information
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, Long id) {
        super(String.format("%s not found with id: %d", resourceName, id));
    }
}
