package com.example.linter.rule;

import com.example.linter.config.Severity;

import java.util.Objects;

public final class OccurrenceRule {
    private final Integer order;
    private final int min;
    private final int max;
    private final Severity severity;

    private OccurrenceRule(Builder builder) {
        this.order = builder.order;
        this.min = builder.min;
        this.max = builder.max;
        this.severity = builder.severity;
    }

    public Integer order() { return order; }
    public int min() { return min; }
    public int max() { return max; }
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer order;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private Severity severity;

        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public OccurrenceRule build() {
            return new OccurrenceRule(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OccurrenceRule that = (OccurrenceRule) o;
        return min == that.min &&
               max == that.max &&
               Objects.equals(order, that.order) &&
               severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(order, min, max, severity);
    }
}