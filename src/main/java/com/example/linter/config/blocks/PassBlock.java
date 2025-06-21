package com.example.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for pass blocks (passthrough content) in AsciiDoc.
 * Pass blocks are delimited by ++++ and pass content through without processing.
 * 
 * <p>This validator supports custom attributes that are not native to AsciiDoc:
 * <ul>
 *   <li>{@code pass-type}: Specifies the content type (html, xml, svg)</li>
 *   <li>{@code pass-reason}: Provides justification for using raw passthrough</li>
 * </ul>
 * 
 * <p>Example usage:
 * <pre>
 * [pass,pass-type=html,pass-reason="Custom widget for product gallery"]
 * ++++
 * &lt;div class="product-slider"&gt;
 *   &lt;img src="product1.jpg" alt="Product 1"&gt;
 * &lt;/div&gt;
 * ++++
 * </pre>
 * 
 * <p>Validation is based on the YAML schema configuration for pass blocks.
 */
@JsonDeserialize(builder = PassBlock.Builder.class)
public final class PassBlock extends AbstractBlock {
    @JsonProperty("type")
    private final TypeConfig type;
    @JsonProperty("content")
    private final ContentConfig content;
    @JsonProperty("justification")
    private final JustificationConfig justification;
    
    private PassBlock(Builder builder) {
        super(builder);
        this.type = builder.type;
        this.content = builder.content;
        this.justification = builder.justification;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.PASS;
    }
    
    public TypeConfig getTypeConfig() {
        return type;
    }
    
    public ContentConfig getContent() {
        return content;
    }
    
    public JustificationConfig getJustification() {
        return justification;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = TypeConfig.TypeConfigBuilder.class)
    public static class TypeConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("allowed")
        private final List<String> allowed;
        @JsonProperty("severity")
        private final Severity severity;
        
        private TypeConfig(TypeConfigBuilder builder) {
            this.required = builder.required;
            this.allowed = builder.allowed != null ? 
                Collections.unmodifiableList(new ArrayList<>(builder.allowed)) : 
                Collections.emptyList();
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public List<String> getAllowed() {
            return allowed;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static TypeConfigBuilder builder() {
            return new TypeConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class TypeConfigBuilder {
            private boolean required;
            private List<String> allowed;
            private Severity severity;
            
            public TypeConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public TypeConfigBuilder allowed(List<String> allowed) {
                this.allowed = allowed;
                return this;
            }
            
            public TypeConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TypeConfig build() {
                return new TypeConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeConfig that)) return false;
            return required == that.required &&
                   Objects.equals(allowed, that.allowed) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
    }
    
    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder.required;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class ContentConfigBuilder {
            private boolean required;
            private Integer maxLength;
            private Pattern pattern;
            private Severity severity;
            
            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public ContentConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public ContentConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public ContentConfig build() {
                return new ContentConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContentConfig that)) return false;
            return required == that.required &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, maxLength, 
                pattern == null ? null : pattern.pattern(), severity);
        }
    }
    
    @JsonDeserialize(builder = JustificationConfig.JustificationConfigBuilder.class)
    public static class JustificationConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("severity")
        private final Severity severity;
        
        private JustificationConfig(JustificationConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static JustificationConfigBuilder builder() {
            return new JustificationConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class JustificationConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public JustificationConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public JustificationConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public JustificationConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public JustificationConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public JustificationConfig build() {
                return new JustificationConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JustificationConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private TypeConfig type;
        private ContentConfig content;
        private JustificationConfig justification;
        
        public Builder type(TypeConfig type) {
            this.type = type;
            return this;
        }
        
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        public Builder justification(JustificationConfig justification) {
            this.justification = justification;
            return this;
        }
        
        @Override
        public PassBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new PassBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PassBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(type, that.type) &&
               Objects.equals(content, that.content) &&
               Objects.equals(justification, that.justification);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, content, justification);
    }
}