package com.naza;

/**
 * This class represents an exception that is thrown when there is an
 * authentication error.
 */
public class AuthError extends Exception {
    /**
     * Constructs a new AuthError with the specified error message.
     *
     * @param errorMessage the error message associated with the exception
     */
    public AuthError(String errorMessage) {
        super(errorMessage);
    }
}
