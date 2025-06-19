package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;

public final class TableBlock extends AbstractBlock {
    
    private TableBlock(Builder builder) {
        super(builder);
    }
    
    @Override
    public BlockType getType() {
        return BlockType.TABLE;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        
        @Override
        public TableBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new TableBlock(this);
        }
    }
}