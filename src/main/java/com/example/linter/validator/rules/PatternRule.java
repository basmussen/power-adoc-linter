package com.example.linter.validator.rules;

import com.example.linter.config.Severity;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.SourceLocation;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public final class PatternRule implements AttributeRule {
    private final Map<String, PatternConfig> patternConfigs;

    private PatternRule(Builder builder) {
        this.patternConfigs = Collections.unmodifiableMap(new HashMap<>(builder.patternConfigs));
    }

    @Override
    public String getRuleId() {
        return "metadata.pattern";
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        PatternConfig config = patternConfigs.get(attributeName);
        if (config != null && value != null && !value.isEmpty()) {
            if (!config.getPattern().matcher(value).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(config.getSeverity())
                    .ruleId(getRuleId())
                    .message("Attribute '" + attributeName + "' does not match required pattern: actual '" + value + "', expected pattern '" + config.getPatternString() + "'")
                    .location(location)
                    .attributeName(attributeName)
                    .actualValue(value)
                    .expectedValue("Pattern '" + config.getPatternString() + "'")
                    .build());
            }
        }
        
        return messages;
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return patternConfigs.containsKey(attributeName);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, PatternConfig> patternConfigs = new HashMap<>();

        private Builder() {
        }

        public Builder addPattern(String attributeName, String pattern, Severity severity) {
            Objects.requireNonNull(attributeName, "attributeName must not be null");
            Objects.requireNonNull(pattern, "pattern must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
            
            try {
                Pattern compiledPattern = Pattern.compile(pattern);
                patternConfigs.put(attributeName, new PatternConfig(compiledPattern, pattern, severity));
            } catch (PatternSyntaxException e) {
                throw new IllegalArgumentException("Invalid pattern for attribute '" + attributeName + "': " + e.getMessage());
            }
            
            return this;
        }

        public PatternRule build() {
            return new PatternRule(this);
        }
    }

    private static final class PatternConfig {
        private final Pattern pattern;
        private final String patternString;
        private final Severity severity;

        PatternConfig(Pattern pattern, String patternString, Severity severity) {
            this.pattern = pattern;
            this.patternString = patternString;
            this.severity = severity;
        }

        Pattern getPattern() {
            return pattern;
        }

        String getPatternString() {
            return patternString;
        }

        Severity getSeverity() {
            return severity;
        }
    }
}