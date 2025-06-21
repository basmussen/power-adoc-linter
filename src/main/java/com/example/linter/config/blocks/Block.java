package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonProperty;

public interface Block {
    @JsonProperty("type")
    BlockType getType();
    
    @JsonProperty("name")
    String getName();
    
    @JsonProperty("severity")
    Severity getSeverity();
    
    @JsonProperty("occurrence")
    OccurrenceConfig getOccurrence();
}