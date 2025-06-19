package com.example.linter.config;

import java.util.Objects;

/**
 * @deprecated Use specific block classes from com.example.linter.config.blocks package instead
 */
@Deprecated
public final class AllowedBlock {
    private final BlockType type;
    private final String name;
    private final Severity severity;
    private final OccurrenceRule occurrence;
    private final LineRule lines;

    private AllowedBlock(Builder builder) {
        this.type = builder.type;
        this.name = builder.name;
        this.severity = builder.severity;
        this.occurrence = builder.occurrence;
        this.lines = builder.lines;
    }

    public BlockType type() { return type; }
    public String name() { return name; }
    public Severity severity() { return severity; }
    public OccurrenceRule occurrence() { return occurrence; }
    public LineRule lines() { return lines; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private BlockType type;
        private String name;
        private Severity severity;
        private OccurrenceRule occurrence;
        private LineRule lines;

        public Builder type(BlockType type) {
            this.type = type;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder occurrence(OccurrenceRule occurrence) {
            this.occurrence = occurrence;
            return this;
        }

        public Builder lines(LineRule lines) {
            this.lines = lines;
            return this;
        }

        public AllowedBlock build() {
            Objects.requireNonNull(type, "type is required");
            Objects.requireNonNull(severity, "severity is required");
            return new AllowedBlock(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AllowedBlock that = (AllowedBlock) o;
        return type == that.type &&
               Objects.equals(name, that.name) &&
               severity == that.severity &&
               Objects.equals(occurrence, that.occurrence) &&
               Objects.equals(lines, that.lines);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, severity, occurrence, lines);
    }
}