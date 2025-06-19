package com.example.linter.config;

import java.util.Objects;

public final class TitleRule {
    private final String pattern;

    private TitleRule(Builder builder) {
        this.pattern = builder.pattern;
    }

    public String pattern() { return pattern; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String pattern;

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public TitleRule build() {
            Objects.requireNonNull(pattern, "pattern is required");
            return new TitleRule(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleRule titleRule = (TitleRule) o;
        return Objects.equals(pattern, titleRule.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}