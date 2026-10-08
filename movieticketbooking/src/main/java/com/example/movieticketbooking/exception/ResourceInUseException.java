package com.example.movieticketbooking.exception;

/** Thrown when a record cannot be deleted because other records still reference it. */
public class ResourceInUseException extends RuntimeException {

    public ResourceInUseException(String message) {
        super(message);
    }
}
