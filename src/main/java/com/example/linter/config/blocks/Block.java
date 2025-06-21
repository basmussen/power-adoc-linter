package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

// No polymorphic annotations here - we'll use custom deserializer
public interface Block {
    @JsonIgnore
    BlockType getType();
    
    @JsonProperty("name")
    String getName();
    
    @JsonProperty("severity")
    Severity getSeverity();
    
    @JsonProperty("occurrence")
    OccurrenceConfig getOccurrence();
}