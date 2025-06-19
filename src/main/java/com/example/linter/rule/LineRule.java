package com.example.linter.rule;

import com.example.linter.config.Severity;
import java.util.Objects;

public final class LineRule {
    private final Integer min;
    private final Integer max;
    private final Severity severity;

    private LineRule(Builder builder) {
        this.min = builder.min;
        this.max = builder.max;
        this.severity = builder.severity;
    }

    public Integer min() { return min; }
    public Integer max() { return max; }
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer min;
        private Integer max;
        private Severity severity;

        public Builder min(Integer min) {
            this.min = min;
            return this;
        }

        public Builder max(Integer max) {
            this.max = max;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public LineRule build() {
            return new LineRule(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineRule lineRule = (LineRule) o;
        return Objects.equals(min, lineRule.min) &&
               Objects.equals(max, lineRule.max) &&
               severity == lineRule.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max, severity);
    }
}