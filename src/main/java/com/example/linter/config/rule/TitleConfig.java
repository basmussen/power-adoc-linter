package com.example.linter.config.rule;

import java.util.Objects;

import com.example.linter.config.Severity;

public final class TitleConfig {
    private final String pattern;
    private final String exactMatch;
    private final Severity severity;

    private TitleConfig(Builder builder) {
        this.pattern = builder.pattern;
        this.exactMatch = builder.exactMatch;
        this.severity = builder.severity;
    }

    public String pattern() { return pattern; }
    public String exactMatch() { return exactMatch; }
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pattern;
        private String exactMatch;
        private Severity severity = Severity.ERROR;

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder exactMatch(String exactMatch) {
            this.exactMatch = exactMatch;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = Objects.requireNonNull(severity, "severity must not be null");
            return this;
        }

        public TitleConfig build() {
            if (pattern == null && exactMatch == null) {
                throw new IllegalStateException("Either pattern or exactMatch must be specified");
            }
            if (pattern != null && exactMatch != null) {
                throw new IllegalStateException("Cannot specify both pattern and exactMatch");
            }
            return new TitleConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleConfig that = (TitleConfig) o;
        return Objects.equals(pattern, that.pattern) &&
               Objects.equals(exactMatch, that.exactMatch) &&
               severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern, exactMatch, severity);
    }
}