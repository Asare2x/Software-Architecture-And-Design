package com.shareanalysis.exception;

/**
 * Thrown when the external data provider (e.g. Yahoo Finance / Data Market) fails.
 */
public class DataProviderException extends Exception {
    public DataProviderException(String message) { super(message); }
    public DataProviderException(String message, Throwable cause) { super(message, cause); }
}
