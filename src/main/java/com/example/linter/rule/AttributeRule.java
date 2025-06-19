package com.example.linter.rule;

import com.example.linter.config.Severity;

import java.util.Objects;

public final class AttributeRule {
    private final String name;
    private final Integer order;
    private final boolean required;
    private final Integer minLength;
    private final Integer maxLength;
    private final String pattern;
    private final Severity severity;

    private AttributeRule(Builder builder) {
        this.name = builder.name;
        this.order = builder.order;
        this.required = builder.required;
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.pattern = builder.pattern;
        this.severity = builder.severity;
    }

    public String name() { return name; }
    public Integer order() { return order; }
    public boolean required() { return required; }
    public Integer minLength() { return minLength; }
    public Integer maxLength() { return maxLength; }
    public String pattern() { return pattern; }
    public Severity severity() { return severity; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Integer order;
        private boolean required;
        private Integer minLength;
        private Integer maxLength;
        private String pattern;
        private Severity severity;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public Builder required(boolean required) {
            this.required = required;
            return this;
        }

        public Builder minLength(Integer minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder maxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public AttributeRule build() {
            Objects.requireNonNull(name, "name is required");
            Objects.requireNonNull(severity, "severity is required");
            return new AttributeRule(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AttributeRule that = (AttributeRule) o;
        return required == that.required &&
               Objects.equals(name, that.name) &&
               Objects.equals(order, that.order) &&
               Objects.equals(minLength, that.minLength) &&
               Objects.equals(maxLength, that.maxLength) &&
               Objects.equals(pattern, that.pattern) &&
               severity == that.severity;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, required, minLength, maxLength, pattern, severity);
    }
}