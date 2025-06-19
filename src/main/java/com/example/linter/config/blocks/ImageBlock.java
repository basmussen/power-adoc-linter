package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;
import java.util.regex.Pattern;

public final class ImageBlock extends AbstractBlock {
    private final UrlRule url;
    private final DimensionRule height;
    private final DimensionRule width;
    private final AltTextRule alt;
    
    private ImageBlock(Builder builder) {
        super(builder);
        this.url = builder.url;
        this.height = builder.height;
        this.width = builder.width;
        this.alt = builder.alt;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.IMAGE;
    }
    
    public UrlRule getUrl() {
        return url;
    }
    
    public DimensionRule getHeight() {
        return height;
    }
    
    public DimensionRule getWidth() {
        return width;
    }
    
    public AltTextRule getAlt() {
        return alt;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class UrlRule {
        private final Pattern pattern;
        private final boolean required;
        
        private UrlRule(UrlRuleBuilder builder) {
            this.pattern = builder.pattern;
            this.required = builder.required;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static UrlRuleBuilder builder() {
            return new UrlRuleBuilder();
        }
        
        public static class UrlRuleBuilder {
            private Pattern pattern;
            private boolean required;
            
            public UrlRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public UrlRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public UrlRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public UrlRule build() {
                return new UrlRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UrlRule that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern());
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(pattern == null ? null : pattern.pattern(), required);
        }
    }
    
    public static class DimensionRule {
        private final Integer minValue;
        private final Integer maxValue;
        private final boolean required;
        
        private DimensionRule(DimensionRuleBuilder builder) {
            this.minValue = builder.minValue;
            this.maxValue = builder.maxValue;
            this.required = builder.required;
        }
        
        public Integer getMinValue() {
            return minValue;
        }
        
        public Integer getMaxValue() {
            return maxValue;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public static DimensionRuleBuilder builder() {
            return new DimensionRuleBuilder();
        }
        
        public static class DimensionRuleBuilder {
            private Integer minValue;
            private Integer maxValue;
            private boolean required;
            
            public DimensionRuleBuilder minValue(Integer minValue) {
                this.minValue = minValue;
                return this;
            }
            
            public DimensionRuleBuilder maxValue(Integer maxValue) {
                this.maxValue = maxValue;
                return this;
            }
            
            public DimensionRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public DimensionRule build() {
                return new DimensionRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionRule that)) return false;
            return required == that.required &&
                   Objects.equals(minValue, that.minValue) &&
                   Objects.equals(maxValue, that.maxValue);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minValue, maxValue, required);
        }
    }
    
    public static class AltTextRule {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        
        private AltTextRule(AltTextRuleBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
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
        
        public static AltTextRuleBuilder builder() {
            return new AltTextRuleBuilder();
        }
        
        public static class AltTextRuleBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            
            public AltTextRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public AltTextRuleBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public AltTextRuleBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public AltTextRule build() {
                return new AltTextRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AltTextRule that)) return false;
            return required == that.required &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength);
        }
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private UrlRule url;
        private DimensionRule height;
        private DimensionRule width;
        private AltTextRule alt;
        
        public Builder url(UrlRule url) {
            this.url = url;
            return this;
        }
        
        public Builder height(DimensionRule height) {
            this.height = height;
            return this;
        }
        
        public Builder width(DimensionRule width) {
            this.width = width;
            return this;
        }
        
        public Builder alt(AltTextRule alt) {
            this.alt = alt;
            return this;
        }
        
        @Override
        public ImageBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new ImageBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImageBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(url, that.url) &&
               Objects.equals(height, that.height) &&
               Objects.equals(width, that.width) &&
               Objects.equals(alt, that.alt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, height, width, alt);
    }
}