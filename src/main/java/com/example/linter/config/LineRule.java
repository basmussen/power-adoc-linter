package com.example.linter.config;

public record LineRule(
    Integer min,
    Integer max,
    Severity severity
) {}