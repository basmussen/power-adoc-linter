package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;

public interface Block {
    BlockType getType();
    String getName();
    Severity getSeverity();
    OccurrenceConfig getOccurrence();
}