package com.example.linter.validator;

import java.util.Objects;
import java.util.Optional;

import com.example.linter.config.Severity;

public final class ValidationMessage {
    private final Severity severity;
    private final String ruleId;
    private final String message;
    private final SourceLocation location;
    private final String attributeName;
    private final String actualValue;
    private final String expectedValue;

    private ValidationMessage(Builder builder) {
        this.severity = Objects.requireNonNull(builder.severity, "severity must not be null");
        this.ruleId = Objects.requireNonNull(builder.ruleId, "ruleId must not be null");
        this.message = Objects.requireNonNull(builder.message, "message must not be null");
        this.location = Objects.requireNonNull(builder.location, "location must not be null");
        this.attributeName = builder.attributeName;
        this.actualValue = builder.actualValue;
        this.expectedValue = builder.expectedValue;
    }

    public Severity getSeverity() {
        return severity;
    }

    public String getRuleId() {
        return ruleId;
    }

    public String getMessage() {
        return message;
    }

    public SourceLocation getLocation() {
        return location;
    }

    public Optional<String> getAttributeName() {
        return Optional.ofNullable(attributeName);
    }

    public Optional<String> getActualValue() {
        return Optional.ofNullable(actualValue);
    }

    public Optional<String> getExpectedValue() {
        return Optional.ofNullable(expectedValue);
    }

    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append(location.formatLocation())
          .append(": [")
          .append(severity)
          .append("] ")
          .append(message);
        
        if (actualValue != null || expectedValue != null) {
            sb.append("\n");
            if (actualValue != null) {
                sb.append("  Found: \"").append(actualValue).append("\"");
            }
            if (expectedValue != null) {
                if (actualValue != null) {
                    sb.append("\n");
                }
                sb.append("  Expected: ").append(expectedValue);
            }
        }
        
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ValidationMessage that = (ValidationMessage) o;
        return severity == that.severity &&
                Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(message, that.message) &&
                Objects.equals(location, that.location) &&
                Objects.equals(attributeName, that.attributeName) &&
                Objects.equals(actualValue, that.actualValue) &&
                Objects.equals(expectedValue, that.expectedValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(severity, ruleId, message, location, attributeName, actualValue, expectedValue);
    }

    @Override
    public String toString() {
        return format();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Severity severity;
        private String ruleId;
        private String message;
        private SourceLocation location;
        private String attributeName;
        private String actualValue;
        private String expectedValue;

        private Builder() {
        }

        public Builder severity(Severity severity) {
            this.severity = severity;
            return this;
        }

        public Builder ruleId(String ruleId) {
            this.ruleId = ruleId;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder location(SourceLocation location) {
            this.location = location;
            return this;
        }

        public Builder attributeName(String attributeName) {
            this.attributeName = attributeName;
            return this;
        }

        public Builder actualValue(String actualValue) {
            this.actualValue = actualValue;
            return this;
        }

        public Builder expectedValue(String expectedValue) {
            this.expectedValue = expectedValue;
            return this;
        }

        public ValidationMessage build() {
            return new ValidationMessage(this);
        }
    }
}