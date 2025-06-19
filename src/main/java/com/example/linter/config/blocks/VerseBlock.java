package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;

public final class VerseBlock extends AbstractBlock {
    
    private VerseBlock(Builder builder) {
        super(builder);
    }
    
    @Override
    public BlockType getType() {
        return BlockType.VERSE;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        
        @Override
        public VerseBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new VerseBlock(this);
        }
    }
}