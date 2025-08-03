package org.example.notearchive.exception;

public class MyLinkException extends Exception {
    public MyLinkException(String message, Exception e) {
        super(message, e);
    }
}
