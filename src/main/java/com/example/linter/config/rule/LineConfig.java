package com.example.linter.config.rule;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = LineConfig.Builder.class)
public final class LineConfig {
    private final Integer min;
    private final Integer max;

    private LineConfig(Builder builder) {
        this.min = builder.min;
        this.max = builder.max;
    }

    @JsonProperty("min")
    public Integer min() { return min; }
    
    @JsonProperty("max")
    public Integer max() { return max; }

    public static Builder builder() {
        return new Builder();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private Integer min;
        private Integer max;

        @JsonProperty("min")
        public Builder min(Integer min) {
            this.min = min;
            return this;
        }

        @JsonProperty("max")
        public Builder max(Integer max) {
            this.max = max;
            return this;
        }

        public LineConfig build() {
            return new LineConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LineConfig lineRule = (LineConfig) o;
        return Objects.equals(min, lineRule.min) &&
               Objects.equals(max, lineRule.max);
    }

    @Override
    public int hashCode() {
        return Objects.hash(min, max);
    }
}