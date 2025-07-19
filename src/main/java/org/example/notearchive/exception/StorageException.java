package org.example.notearchive.exception;

public class StorageException extends Exception {
    public StorageException(String message, Exception e) {
        super(message, e);
    }
}
