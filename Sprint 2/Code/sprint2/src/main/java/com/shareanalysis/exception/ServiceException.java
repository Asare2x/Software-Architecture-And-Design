package com.shareanalysis.exception;

/**
 * Thrown when a service component cannot complete its operation.
 */
public class ServiceException extends Exception {
    public ServiceException(String message) { super(message); }
    public ServiceException(String message, Throwable cause) { super(message, cause); }
}
