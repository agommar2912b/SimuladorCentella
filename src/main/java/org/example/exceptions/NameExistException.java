package org.example.exceptions;

public class NameExistException extends RuntimeException {
    public NameExistException(String message) {
        super(message);
    }
}
