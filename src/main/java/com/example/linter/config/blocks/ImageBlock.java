package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;

public final class ImageBlock extends AbstractBlock {
    
    private ImageBlock(Builder builder) {
        super(builder);
    }
    
    @Override
    public BlockType getType() {
        return BlockType.IMAGE;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        
        @Override
        public ImageBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new ImageBlock(this);
        }
    }
}