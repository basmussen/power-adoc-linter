package com.example.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = VerseBlock.Builder.class)
public final class VerseBlock extends AbstractBlock {
    private final AuthorConfig author;
    private final AttributionConfig attribution;
    private final ContentConfig content;
    
    private VerseBlock(Builder builder) {
        super(builder);
        this.author = builder.author;
        this.attribution = builder.attribution;
        this.content = builder.content;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.VERSE;
    }
    
    @JsonProperty("author")
    public AuthorConfig getAuthor() {
        return author;
    }
    
    @JsonProperty("attribution")
    public AttributionConfig getAttribution() {
        return attribution;
    }
    
    @JsonProperty("content")
    public ContentConfig getContent() {
        return content;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = AuthorConfig.AuthorConfigBuilder.class)
    public static class AuthorConfig {
        private final String defaultValue;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        private final Severity severity;
        
        private AuthorConfig(AuthorConfigBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
            this.severity = builder.severity;
        }
        
        @JsonProperty("defaultValue")
        public String getDefaultValue() {
            return defaultValue;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static AuthorConfigBuilder builder() {
            return new AuthorConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AuthorConfigBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            private Severity severity;
            
            @JsonProperty("defaultValue")
            public AuthorConfigBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            @JsonProperty("minLength")
            public AuthorConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public AuthorConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AuthorConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public AuthorConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("required")
            public AuthorConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public AuthorConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public AuthorConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new AuthorConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AuthorConfig that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required, severity);
        }
    }
    
    @JsonDeserialize(builder = AttributionConfig.AttributionConfigBuilder.class)
    public static class AttributionConfig {
        private final String defaultValue;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        private final Severity severity;
        
        private AttributionConfig(AttributionConfigBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
            this.severity = builder.severity;
        }
        
        @JsonProperty("defaultValue")
        public String getDefaultValue() {
            return defaultValue;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static AttributionConfigBuilder builder() {
            return new AttributionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AttributionConfigBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            private Severity severity;
            
            @JsonProperty("defaultValue")
            public AttributionConfigBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            @JsonProperty("minLength")
            public AttributionConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public AttributionConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AttributionConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public AttributionConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("required")
            public AttributionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public AttributionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public AttributionConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new AttributionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required, severity);
        }
    }
    
    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        private final Severity severity;
        
        private ContentConfig(ContentConfigBuilder builder) {
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
            this.severity = builder.severity;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static ContentConfigBuilder builder() {
            return new ContentConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class ContentConfigBuilder {
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            private Severity severity;
            
            @JsonProperty("minLength")
            public ContentConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public ContentConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("required")
            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public ContentConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public ContentConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new ContentConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContentConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private AuthorConfig author;
        private AttributionConfig attribution;
        private ContentConfig content;
        
        @JsonProperty("author")
        public Builder author(AuthorConfig author) {
            this.author = author;
            return this;
        }
        
        @JsonProperty("attribution")
        public Builder attribution(AttributionConfig attribution) {
            this.attribution = attribution;
            return this;
        }
        
        @JsonProperty("content")
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        @Override
        public VerseBlock build() {
            if (severity == null) {
                severity = Severity.WARN;
            }
            return new VerseBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VerseBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(author, that.author) &&
               Objects.equals(attribution, that.attribution) &&
               Objects.equals(content, that.content);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, attribution, content);
    }
}