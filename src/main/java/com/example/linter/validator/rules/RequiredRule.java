package com.example.linter.validator.rules;

import com.example.linter.config.Severity;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.SourceLocation;
import java.util.*;

public final class RequiredRule implements AttributeRule {
    private final Map<String, RequiredAttribute> requiredAttributes;

    private RequiredRule(Builder builder) {
        this.requiredAttributes = Collections.unmodifiableMap(new HashMap<>(builder.requiredAttributes));
    }

    @Override
    public String getRuleId() {
        return "metadata.required";
    }

    @Override
    public List<ValidationMessage> validate(String attributeName, String value, SourceLocation location) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        RequiredAttribute config = requiredAttributes.get(attributeName);
        if (config != null && config.isRequired() && (value == null || value.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId(getRuleId())
                .message("Missing required attribute '" + attributeName + "'")
                .location(location)
                .attributeName(attributeName)
                .build());
        }
        
        return messages;
    }

    @Override
    public boolean isApplicable(String attributeName) {
        return requiredAttributes.containsKey(attributeName);
    }

    public List<ValidationMessage> validateMissingAttributes(Set<String> presentAttributes, SourceLocation documentLocation) {
        List<ValidationMessage> messages = new ArrayList<>();
        
        for (Map.Entry<String, RequiredAttribute> entry : requiredAttributes.entrySet()) {
            String attrName = entry.getKey();
            RequiredAttribute config = entry.getValue();
            
            if (config.isRequired() && !presentAttributes.contains(attrName)) {
                messages.add(ValidationMessage.builder()
                    .severity(config.getSeverity())
                    .ruleId(getRuleId())
                    .message("Missing required attribute '" + attrName + "'")
                    .location(documentLocation)
                    .attributeName(attrName)
                    .build());
            }
        }
        
        return messages;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private final Map<String, RequiredAttribute> requiredAttributes = new HashMap<>();

        private Builder() {
        }

        public Builder addAttribute(String name, boolean required, Severity severity) {
            Objects.requireNonNull(name, "name must not be null");
            Objects.requireNonNull(severity, "severity must not be null");
            requiredAttributes.put(name, new RequiredAttribute(required, severity));
            return this;
        }

        public RequiredRule build() {
            return new RequiredRule(this);
        }
    }

    private static final class RequiredAttribute {
        private final boolean required;
        private final Severity severity;

        RequiredAttribute(boolean required, Severity severity) {
            this.required = required;
            this.severity = severity;
        }

        boolean isRequired() {
            return required;
        }

        Severity getSeverity() {
            return severity;
        }
    }
}