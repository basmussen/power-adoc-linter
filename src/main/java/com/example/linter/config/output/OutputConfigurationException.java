package com.example.linter.config.output;

/**
 * Exception thrown when output configuration loading or validation fails.
 */
public class OutputConfigurationException extends RuntimeException {
    
    public OutputConfigurationException(String message) {
        super(message);
    }
    
    public OutputConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}