package com.example.linter.config;

import java.util.List;

public record MetadataConfiguration(
    List<AttributeRule> attributes
) {}