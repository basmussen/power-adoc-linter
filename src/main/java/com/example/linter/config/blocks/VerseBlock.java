package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;
import java.util.regex.Pattern;

public final class VerseBlock extends AbstractBlock {
    private final AuthorRule author;
    private final AttributionRule attribution;
    private final ContentRule content;
    
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
    
    public AuthorRule getAuthor() {
        return author;
    }
    
    public AttributionRule getAttribution() {
        return attribution;
    }
    
    public ContentRule getContent() {
        return content;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class AuthorRule {
        private final String defaultValue;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        
        private AuthorRule(AuthorRuleBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static AuthorRuleBuilder builder() {
            return new AuthorRuleBuilder();
        }
        
        public static class AuthorRuleBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public AuthorRuleBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            public AuthorRuleBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AuthorRuleBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AuthorRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public AuthorRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public AuthorRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AuthorRule build() {
                return new AuthorRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AuthorRule that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    public static class AttributionRule {
        private final String defaultValue;
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        
        private AttributionRule(AttributionRuleBuilder builder) {
            this.defaultValue = builder.defaultValue;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static AttributionRuleBuilder builder() {
            return new AttributionRuleBuilder();
        }
        
        public static class AttributionRuleBuilder {
            private String defaultValue;
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public AttributionRuleBuilder defaultValue(String defaultValue) {
                this.defaultValue = defaultValue;
                return this;
            }
            
            public AttributionRuleBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AttributionRuleBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AttributionRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public AttributionRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public AttributionRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AttributionRule build() {
                return new AttributionRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AttributionRule that)) return false;
            return required == that.required &&
                   Objects.equals(defaultValue, that.defaultValue) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(defaultValue, minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    public static class ContentRule {
        private final Integer minLength;
        private final Integer maxLength;
        private final Pattern pattern;
        private final boolean required;
        
        private ContentRule(ContentRuleBuilder builder) {
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public Integer getMinLength() {
            return minLength;
        }
        
        public Integer getMaxLength() {
            return maxLength;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static ContentRuleBuilder builder() {
            return new ContentRuleBuilder();
        }
        
        public static class ContentRuleBuilder {
            private Integer minLength;
            private Integer maxLength;
            private Pattern pattern;
            private boolean required;
            
            public ContentRuleBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public ContentRuleBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public ContentRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public ContentRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public ContentRule build() {
                return new ContentRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContentRule that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minLength, maxLength,
                               pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private AuthorRule author;
        private AttributionRule attribution;
        private ContentRule content;
        
        public Builder author(AuthorRule author) {
            this.author = author;
            return this;
        }
        
        public Builder attribution(AttributionRule attribution) {
            this.attribution = attribution;
            return this;
        }
        
        public Builder content(ContentRule content) {
            this.content = content;
            return this;
        }
        
        @Override
        public VerseBlock build() {
            Objects.requireNonNull(severity, "severity is required");
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