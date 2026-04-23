package com.example.trading.exception;

/**
 * Thrown when an external data provider fails to return share price data.
 */
public class DataProviderException extends RuntimeException {

    public DataProviderException(String message) {
        super(message);
    }

    public DataProviderException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataProviderException(Throwable cause) {
        super(cause);
    }
}
