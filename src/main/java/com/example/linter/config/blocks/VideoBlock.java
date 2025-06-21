package com.example.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

/**
 * Configuration for video blocks in AsciiDoc documents.
 * 
 * <p>Validates video blocks based on the YAML schema structure where
 * block types are keys and their configurations are nested objects.</p>
 * 
 * <p>Example YAML configuration:
 * <pre>
 * - video:
 *     severity: warn
 *     occurrence:
 *       min: 0
 *       max: 2
 *     url:
 *       required: true
 *       pattern: "^(https?://|\\./|/).*\\.(mp4|webm|ogg|avi|mov)$|^https://(www\\.)?(youtube\\.com|vimeo\\.com)/.*"
 *     width:
 *       minValue: 320
 *       maxValue: 1920
 *     options:
 *       autoplay:
 *         allowed: false
 *       controls:
 *         required: true
 * </pre>
 */
@JsonDeserialize(builder = VideoBlock.Builder.class)
public final class VideoBlock extends AbstractBlock {
    @JsonProperty("url")
    private final UrlConfig url;
    @JsonProperty("width")
    private final DimensionConfig width;
    @JsonProperty("height")
    private final DimensionConfig height;
    @JsonProperty("poster")
    private final PosterConfig poster;
    @JsonProperty("options")
    private final OptionsConfig options;
    @JsonProperty("caption")
    private final CaptionConfig caption;
    
    private VideoBlock(Builder builder) {
        super(builder);
        this.url = builder.url;
        this.width = builder.width;
        this.height = builder.height;
        this.poster = builder.poster;
        this.options = builder.options;
        this.caption = builder.caption;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.VIDEO;
    }
    
    public UrlConfig getUrl() {
        return url;
    }
    
    public DimensionConfig getWidth() {
        return width;
    }
    
    public DimensionConfig getHeight() {
        return height;
    }
    
    public PosterConfig getPoster() {
        return poster;
    }
    
    public OptionsConfig getOptions() {
        return options;
    }
    
    public CaptionConfig getCaption() {
        return caption;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!super.equals(obj)) return false;
        if (!(obj instanceof VideoBlock)) return false;
        VideoBlock other = (VideoBlock) obj;
        return Objects.equals(url, other.url) &&
               Objects.equals(width, other.width) &&
               Objects.equals(height, other.height) &&
               Objects.equals(poster, other.poster) &&
               Objects.equals(options, other.options) &&
               Objects.equals(caption, other.caption);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), url, width, height, poster, options, caption);
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBlock.AbstractBuilder<Builder> {
        private UrlConfig url;
        private DimensionConfig width;
        private DimensionConfig height;
        private PosterConfig poster;
        private OptionsConfig options;
        private CaptionConfig caption;
        
        @JsonProperty("url")
        public Builder url(UrlConfig url) {
            this.url = url;
            return this;
        }
        
        @JsonProperty("width")
        public Builder width(DimensionConfig width) {
            this.width = width;
            return this;
        }
        
        @JsonProperty("height")
        public Builder height(DimensionConfig height) {
            this.height = height;
            return this;
        }
        
        @JsonProperty("poster")
        public Builder poster(PosterConfig poster) {
            this.poster = poster;
            return this;
        }
        
        @JsonProperty("options")
        public Builder options(OptionsConfig options) {
            this.options = options;
            return this;
        }
        
        @JsonProperty("caption")
        public Builder caption(CaptionConfig caption) {
            this.caption = caption;
            return this;
        }
        
        @Override
        public VideoBlock build() {
            return new VideoBlock(this);
        }
        
    }
    
    /**
     * Configuration for video URL validation.
     */
    @JsonDeserialize(builder = UrlConfig.UrlConfigBuilder.class)
    public static class UrlConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private UrlConfig(UrlConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            UrlConfig other = (UrlConfig) obj;
            return required == other.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(), 
                                 other.pattern == null ? null : other.pattern.pattern()) &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
        
        public static UrlConfigBuilder builder() {
            return new UrlConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class UrlConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            @JsonProperty("required")
            public UrlConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("pattern")
            public UrlConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public UrlConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public UrlConfig build() {
                return new UrlConfig(this);
            }
        }
    }
    
    /**
     * Configuration for video dimension validation (width/height).
     */
    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minValue")
        private final Integer minValue;
        @JsonProperty("maxValue")
        private final Integer maxValue;
        @JsonProperty("severity")
        private final Severity severity;
        
        private DimensionConfig(DimensionConfigBuilder builder) {
            this.required = builder.required;
            this.minValue = builder.minValue;
            this.maxValue = builder.maxValue;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Integer getMinValue() {
            return minValue;
        }
        
        public Integer getMaxValue() {
            return maxValue;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            DimensionConfig other = (DimensionConfig) obj;
            return required == other.required &&
                   Objects.equals(minValue, other.minValue) &&
                   Objects.equals(maxValue, other.maxValue) &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minValue, maxValue, severity);
        }
        
        public static DimensionConfigBuilder builder() {
            return new DimensionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class DimensionConfigBuilder {
            private boolean required;
            private Integer minValue;
            private Integer maxValue;
            private Severity severity;
            
            @JsonProperty("required")
            public DimensionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
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
            
            @JsonProperty("severity")
            public DimensionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public DimensionConfig build() {
                return new DimensionConfig(this);
            }
        }
    }
    
    /**
     * Configuration for video poster image validation.
     */
    @JsonDeserialize(builder = PosterConfig.PosterConfigBuilder.class)
    public static class PosterConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private PosterConfig(PosterConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Pattern getPattern() {
            return pattern;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PosterConfig other = (PosterConfig) obj;
            return required == other.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(), 
                                 other.pattern == null ? null : other.pattern.pattern()) &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
        
        public static PosterConfigBuilder builder() {
            return new PosterConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class PosterConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            @JsonProperty("required")
            public PosterConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("pattern")
            public PosterConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public PosterConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public PosterConfig build() {
                return new PosterConfig(this);
            }
        }
    }
    
    /**
     * Configuration for video playback options.
     */
    @JsonDeserialize(builder = OptionsConfig.OptionsConfigBuilder.class)
    public static class OptionsConfig {
        @JsonProperty("autoplay")
        private final AutoplayConfig autoplay;
        @JsonProperty("controls")
        private final ControlsConfig controls;
        
        private OptionsConfig(OptionsConfigBuilder builder) {
            this.autoplay = builder.autoplay;
            this.controls = builder.controls;
        }
        
        public AutoplayConfig getAutoplay() {
            return autoplay;
        }
        
        public ControlsConfig getControls() {
            return controls;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            OptionsConfig other = (OptionsConfig) obj;
            return Objects.equals(autoplay, other.autoplay) &&
                   Objects.equals(controls, other.controls);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(autoplay, controls);
        }
        
        public static OptionsConfigBuilder builder() {
            return new OptionsConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class OptionsConfigBuilder {
            private AutoplayConfig autoplay;
            private ControlsConfig controls;
            
            @JsonProperty("autoplay")
            public OptionsConfigBuilder autoplay(AutoplayConfig autoplay) {
                this.autoplay = autoplay;
                return this;
            }
            
            @JsonProperty("controls")
            public OptionsConfigBuilder controls(ControlsConfig controls) {
                this.controls = controls;
                return this;
            }
            
            public OptionsConfig build() {
                return new OptionsConfig(this);
            }
        }
    }
    
    /**
     * Configuration for autoplay option.
     */
    @JsonDeserialize(builder = AutoplayConfig.AutoplayConfigBuilder.class)
    public static class AutoplayConfig {
        @JsonProperty("allowed")
        private final Boolean allowed;
        @JsonProperty("severity")
        private final Severity severity;
        
        private AutoplayConfig(AutoplayConfigBuilder builder) {
            this.allowed = builder.allowed;
            this.severity = builder.severity;
        }
        
        public Boolean getAllowed() {
            return allowed;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            AutoplayConfig other = (AutoplayConfig) obj;
            return Objects.equals(allowed, other.allowed) &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(allowed, severity);
        }
        
        public static AutoplayConfigBuilder builder() {
            return new AutoplayConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class AutoplayConfigBuilder {
            private Boolean allowed;
            private Severity severity;
            
            @JsonProperty("allowed")
            public AutoplayConfigBuilder allowed(Boolean allowed) {
                this.allowed = allowed;
                return this;
            }
            
            @JsonProperty("severity")
            public AutoplayConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public AutoplayConfig build() {
                return new AutoplayConfig(this);
            }
        }
    }
    
    /**
     * Configuration for controls option.
     */
    @JsonDeserialize(builder = ControlsConfig.ControlsConfigBuilder.class)
    public static class ControlsConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("severity")
        private final Severity severity;
        
        private ControlsConfig(ControlsConfigBuilder builder) {
            this.required = builder.required;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ControlsConfig other = (ControlsConfig) obj;
            return required == other.required &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, severity);
        }
        
        public static ControlsConfigBuilder builder() {
            return new ControlsConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class ControlsConfigBuilder {
            private boolean required;
            private Severity severity;
            
            @JsonProperty("required")
            public ControlsConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("severity")
            public ControlsConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public ControlsConfig build() {
                return new ControlsConfig(this);
            }
        }
    }
    
    /**
     * Configuration for video caption validation.
     */
    @JsonDeserialize(builder = CaptionConfig.CaptionConfigBuilder.class)
    public static class CaptionConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("severity")
        private final Severity severity;
        
        private CaptionConfig(CaptionConfigBuilder builder) {
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
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            CaptionConfig other = (CaptionConfig) obj;
            return required == other.required &&
                   Objects.equals(minLength, other.minLength) &&
                   Objects.equals(maxLength, other.maxLength) &&
                   severity == other.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, severity);
        }
        
        public static CaptionConfigBuilder builder() {
            return new CaptionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class CaptionConfigBuilder {
            private boolean required;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            @JsonProperty("required")
            public CaptionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("minLength")
            public CaptionConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            @JsonProperty("maxLength")
            public CaptionConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            @JsonProperty("severity")
            public CaptionConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CaptionConfig build() {
                return new CaptionConfig(this);
            }
        }
    }
}