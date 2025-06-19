package com.example.linter.config;

public record OccurrenceRule(
    Integer order,
    int min,
    int max,
    Severity severity
) {}