package com.example.linter.config.blocks;

import java.util.Objects;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;

public abstract class AbstractBlock implements Block {
    private final String name;
    private final Severity severity;
    private final OccurrenceConfig occurrence;
    
    protected AbstractBlock(AbstractBuilder<?> builder) {
        this.name = builder.name;
        this.severity = builder.severity;
        this.occurrence = builder.occurrence;
    }
    
    public abstract BlockType getType();
    
    public String getName() { return name; }
    public Severity getSeverity() { return severity; }
    public OccurrenceConfig getOccurrence() { return occurrence; }
    
    protected abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        protected String name;
        protected Severity severity;
        protected OccurrenceConfig occurrence;
        
        @SuppressWarnings("unchecked")
        public T name(String name) {
            this.name = name;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T severity(Severity severity) {
            this.severity = severity;
            return (T) this;
        }
        
        @SuppressWarnings("unchecked")
        public T occurrence(OccurrenceConfig occurrence) {
            this.occurrence = occurrence;
            return (T) this;
        }
        
        public abstract AbstractBlock build();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return Objects.equals(name, that.name) &&
               severity == that.severity &&
               Objects.equals(occurrence, that.occurrence);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, severity, occurrence);
    }
}