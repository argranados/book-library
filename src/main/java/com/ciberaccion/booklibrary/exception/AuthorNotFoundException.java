package com.ciberaccion.booklibrary.exception;

public class AuthorNotFoundException extends RuntimeException {
    public AuthorNotFoundException(Long id) {
        super("Author not found: " + id);
    }
}