package com.example.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ImageBlock.Builder.class)
public final class ImageBlock extends AbstractBlock {
    private final UrlConfig url;
    private final DimensionConfig height;
    private final DimensionConfig width;
    private final AltTextConfig alt;
    
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
    
    @JsonProperty("url")
    public UrlConfig getUrl() {
        return url;
    }
    
    @JsonProperty("height")
    public DimensionConfig getHeight() {
        return height;
    }
    
    @JsonProperty("width")
    public DimensionConfig getWidth() {
        return width;
    }
    
    @JsonProperty("alt")
    public AltTextConfig getAlt() {
        return alt;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = UrlConfig.UrlConfigBuilder.class)
    public static class UrlConfig {
        private final Pattern pattern;
        private final boolean required;
        private final Severity severity;
        
        private UrlConfig(UrlConfigBuilder builder) {
            this.pattern = builder.pattern;
            this.required = builder.required;
            this.severity = builder.severity;
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
        
        public static UrlConfigBuilder builder() {
            return new UrlConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class UrlConfigBuilder {
            private Pattern pattern;
            private boolean required;
            private Severity severity;
            
            public UrlConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public UrlConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("required")
            public UrlConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public UrlConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public UrlConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new UrlConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof UrlConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(pattern == null ? null : pattern.pattern(), required, severity);
        }
    }
    
    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        private final Integer minValue;
        private final Integer maxValue;
        private final boolean required;
        private final Severity severity;
        
        private DimensionConfig(DimensionConfigBuilder builder) {
            this.minValue = builder.minValue;
            this.maxValue = builder.maxValue;
            this.required = builder.required;
            this.severity = builder.severity;
        }
        
        @JsonProperty("minValue")
        public Integer getMinValue() {
            return minValue;
        }
        
        @JsonProperty("maxValue")
        public Integer getMaxValue() {
            return maxValue;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DimensionConfigBuilder {
            private Integer minValue;
            private Integer maxValue;
            private boolean required;
            private Severity severity;
            
            @JsonProperty("minValue")
            public DimensionConfigBuilder minValue(Integer minValue) {
                this.minValue = minValue;
                return this;
            }
            
            @JsonProperty("maxValue")
            public DimensionConfigBuilder maxValue(Integer maxValue) {
                this.maxValue = maxValue;
                return this;
            }
            
            @JsonProperty("required")
            public DimensionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public DimensionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public DimensionConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new DimensionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof DimensionConfig that)) return false;
            return required == that.required &&
                   Objects.equals(minValue, that.minValue) &&
                   Objects.equals(maxValue, that.maxValue) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(minValue, maxValue, required, severity);
        }
    }
    
    @JsonDeserialize(builder = AltTextConfig.AltTextConfigBuilder.class)
    public static class AltTextConfig {
        private final boolean required;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;
        
        private AltTextConfig(AltTextConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("minLength")
        public Integer getMinLength() {
            return minLength;
        }
        
        @JsonProperty("maxLength")
        public Integer getMaxLength() {
            return maxLength;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static AltTextConfigBuilder builder() {
            return new AltTextConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AltTextConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            @JsonProperty("required")
            public AltTextConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("minLength")
            public AltTextConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public AltTextConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            @JsonProperty("severity")
            public AltTextConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public AltTextConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new AltTextConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof AltTextConfig that)) return false;
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
        private UrlConfig url;
        private DimensionConfig height;
        private DimensionConfig width;
        private AltTextConfig alt;
        
        @JsonProperty("url")
        public Builder url(UrlConfig url) {
            this.url = url;
            return this;
        }
        
        @JsonProperty("height")
        public Builder height(DimensionConfig height) {
            this.height = height;
            return this;
        }
        
        @JsonProperty("width")
        public Builder width(DimensionConfig width) {
            this.width = width;
            return this;
        }
        
        @JsonProperty("alt")
        public Builder alt(AltTextConfig alt) {
            this.alt = alt;
            return this;
        }
        
        @Override
        public ImageBlock build() {
            if (severity == null) {
                severity = Severity.WARN;
            }
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