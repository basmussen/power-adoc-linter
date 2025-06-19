package com.example.linter.config;

import java.util.List;

public record SectionRule(
    String name,
    Integer order,
    int level,
    int min,
    int max,
    TitleRule title,
    List<AllowedBlock> allowedBlocks,
    List<SectionRule> subsections
) {}