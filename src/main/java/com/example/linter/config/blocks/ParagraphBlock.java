package com.example.linter.config.blocks;

import java.util.Objects;

import com.example.linter.config.BlockType;
import com.example.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ParagraphBlock.Builder.class)
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
    
    @JsonProperty("lines")
    public LineConfig getLines() { return lines; }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private LineConfig lines;
        
        @JsonProperty("lines")
        public Builder lines(LineConfig lines) {
            this.lines = lines;
            return this;
        }
        
        @Override
        public ParagraphBlock build() {
            // Default severity if not provided
            if (severity == null) {
                severity = com.example.linter.config.Severity.WARN;
            }
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