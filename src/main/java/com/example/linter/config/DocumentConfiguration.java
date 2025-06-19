package com.example.linter.config;

import java.util.List;

public record DocumentConfiguration(
    MetadataConfiguration metadata,
    List<SectionRule> sections
) {}