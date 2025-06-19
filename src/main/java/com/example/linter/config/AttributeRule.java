package com.example.linter.config;

public record AttributeRule(
    String name,
    Integer order,
    boolean required,
    Integer minLength,
    Integer maxLength,
    String pattern,
    Severity severity
) {}