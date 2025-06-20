package com.example.linter.config.validation;

/**
 * Exception thrown when user configuration does not match the schema.
 */
public class RuleValidationException extends RuntimeException {
    
    public RuleValidationException(String message) {
        super(message);
    }
    
    public RuleValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}