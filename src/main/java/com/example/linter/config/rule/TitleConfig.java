package com.example.linter.config.rule;

import java.util.Objects;

public final class TitleConfig {
    private final String pattern;

    private TitleConfig(Builder builder) {
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

        public TitleConfig build() {
            Objects.requireNonNull(pattern, "pattern is required");
            return new TitleConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TitleConfig titleRule = (TitleConfig) o;
        return Objects.equals(pattern, titleRule.pattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pattern);
    }
}