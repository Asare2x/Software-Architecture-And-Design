package com.shareanalysis.api;

/**
 * Thrown when an external share data provider cannot fulfil a request.
 *
 * Wrapping provider errors in a domain-specific exception keeps
 * higher-level components decoupled from third-party library exceptions.
 */
public class DataProviderException extends Exception {

    public DataProviderException(String message) {
        super(message);
    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
