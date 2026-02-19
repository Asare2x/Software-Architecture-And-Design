package com.shareanalysis.service;

/**
 * Thrown when the service layer cannot fulfil a request.
 *
 * Wraps lower-level exceptions (e.g. DataProviderException, IOException)
 * into a uniform exception type that the UI layer can handle without
 * coupling to infrastructure-level details.
 */
public class ServiceException extends Exception {

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
