package com.example.linter.config.blocks;

import java.util.Objects;

import com.example.linter.config.BlockType;
import com.example.linter.config.rule.LineConfig;

public final class ParagraphBlock extends AbstractBlock {
    private final LineConfig lines;
    
    private ParagraphBlock(Builder builder) {
        super(builder);
        this.lines = builder.lines;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.PARAGRAPH;
    }
    
    public LineConfig getLines() { return lines; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private LineConfig lines;
        
        public Builder lines(LineConfig lines) {
            this.lines = lines;
            return this;
        }
        
        @Override
        public ParagraphBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new ParagraphBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ParagraphBlock that = (ParagraphBlock) o;
        return Objects.equals(lines, that.lines);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), lines);
    }
}