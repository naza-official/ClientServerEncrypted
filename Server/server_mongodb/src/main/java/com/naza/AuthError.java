package com.naza;

public class AuthError extends Exception {
    public AuthError(String errorMessage) {
        super(errorMessage);
    }
}
