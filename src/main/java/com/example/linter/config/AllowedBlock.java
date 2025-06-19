package com.example.linter.config;

public record AllowedBlock(
    BlockType type,
    String name,
    Severity severity,
    OccurrenceRule occurrence,
    LineRule lines
) {}