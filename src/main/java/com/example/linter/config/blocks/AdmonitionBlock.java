package com.example.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = AdmonitionBlock.Builder.class)
public final class AdmonitionBlock extends AbstractBlock {
    @JsonProperty("type")
    private final TypeConfig type;
    @JsonProperty("title")
    private final TitleConfig title;
    @JsonProperty("content")
    private final ContentConfig content;
    @JsonProperty("icon")
    private final IconConfig icon;
    @JsonProperty("NOTE")
    private final TypeOccurrenceConfig noteOccurrence;
    @JsonProperty("TIP")
    private final TypeOccurrenceConfig tipOccurrence;
    @JsonProperty("IMPORTANT")
    private final TypeOccurrenceConfig importantOccurrence;
    @JsonProperty("WARNING")
    private final TypeOccurrenceConfig warningOccurrence;
    @JsonProperty("CAUTION")
    private final TypeOccurrenceConfig cautionOccurrence;
    
    private AdmonitionBlock(Builder builder) {
        super(builder);
        this.type = builder.type;
        this.title = builder.title;
        this.content = builder.content;
        this.icon = builder.icon;
        this.noteOccurrence = builder.noteOccurrence;
        this.tipOccurrence = builder.tipOccurrence;
        this.importantOccurrence = builder.importantOccurrence;
        this.warningOccurrence = builder.warningOccurrence;
        this.cautionOccurrence = builder.cautionOccurrence;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.ADMONITION;
    }
    
    public TypeConfig getTypeConfig() {
        return type;
    }
    
    public TitleConfig getTitle() {
        return title;
    }
    
    public ContentConfig getContent() {
        return content;
    }
    
    public IconConfig getIcon() {
        return icon;
    }
    
    public TypeOccurrenceConfig getNoteOccurrence() {
        return noteOccurrence;
    }
    
    public TypeOccurrenceConfig getTipOccurrence() {
        return tipOccurrence;
    }
    
    public TypeOccurrenceConfig getImportantOccurrence() {
        return importantOccurrence;
    }
    
    public TypeOccurrenceConfig getWarningOccurrence() {
        return warningOccurrence;
    }
    
    public TypeOccurrenceConfig getCautionOccurrence() {
        return cautionOccurrence;
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
    
    @JsonDeserialize(builder = TitleConfig.TitleConfigBuilder.class)
    public static class TitleConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("severity")
        private final Severity severity;
        
        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.severity = builder.severity;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public Pattern getPattern() {
            return pattern;
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
        
        public static TitleConfigBuilder builder() {
            return new TitleConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class TitleConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public TitleConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public TitleConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public TitleConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public TitleConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public TitleConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public TitleConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TitleConfig build() {
                return new TitleConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TitleConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), 
                               minLength, maxLength, severity);
        }
    }
    
    @JsonDeserialize(builder = ContentConfig.ContentConfigBuilder.class)
    public static class ContentConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("minLength")
        private final Integer minLength;
        @JsonProperty("maxLength")
        private final Integer maxLength;
        @JsonProperty("lines")
        private final LineConfig lines;
        @JsonProperty("severity")
        private final Severity severity;
        
        private ContentConfig(ContentConfigBuilder builder) {
            this.required = builder.required;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
            this.lines = builder.lines;
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
        
        public LineConfig getLines() {
            return lines;
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
            private Integer minLength;
            private Integer maxLength;
            private LineConfig lines;
            private Severity severity;
            
            public ContentConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public ContentConfigBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public ContentConfigBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public ContentConfigBuilder lines(LineConfig lines) {
                this.lines = lines;
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
                   Objects.equals(minLength, that.minLength) &&
                   Objects.equals(maxLength, that.maxLength) &&
                   Objects.equals(lines, that.lines) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, minLength, maxLength, lines, severity);
        }
    }
    
    @JsonDeserialize(builder = IconConfig.IconConfigBuilder.class)
    public static class IconConfig {
        @JsonProperty("required")
        private final boolean required;
        @JsonProperty("pattern")
        private final Pattern pattern;
        @JsonProperty("severity")
        private final Severity severity;
        
        private IconConfig(IconConfigBuilder builder) {
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
        
        public static IconConfigBuilder builder() {
            return new IconConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class IconConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            public IconConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public IconConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public IconConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public IconConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public IconConfig build() {
                return new IconConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IconConfig that)) return false;
            return required == that.required &&
                   Objects.equals(pattern == null ? null : pattern.pattern(),
                                 that.pattern == null ? null : that.pattern.pattern()) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }
    
    @JsonDeserialize(builder = TypeOccurrenceConfig.TypeOccurrenceConfigBuilder.class)
    public static class TypeOccurrenceConfig {
        @JsonProperty("max")
        private final Integer max;
        @JsonProperty("severity")
        private final Severity severity;
        
        private TypeOccurrenceConfig(TypeOccurrenceConfigBuilder builder) {
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        public Integer getMax() {
            return max;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static TypeOccurrenceConfigBuilder builder() {
            return new TypeOccurrenceConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class TypeOccurrenceConfigBuilder {
            private Integer max;
            private Severity severity;
            
            public TypeOccurrenceConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public TypeOccurrenceConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TypeOccurrenceConfig build() {
                return new TypeOccurrenceConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TypeOccurrenceConfig that)) return false;
            return Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(max, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private TypeConfig type;
        private TitleConfig title;
        private ContentConfig content;
        private IconConfig icon;
        private TypeOccurrenceConfig noteOccurrence;
        private TypeOccurrenceConfig tipOccurrence;
        private TypeOccurrenceConfig importantOccurrence;
        private TypeOccurrenceConfig warningOccurrence;
        private TypeOccurrenceConfig cautionOccurrence;
        
        public Builder type(TypeConfig type) {
            this.type = type;
            return this;
        }
        
        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }
        
        public Builder content(ContentConfig content) {
            this.content = content;
            return this;
        }
        
        public Builder icon(IconConfig icon) {
            this.icon = icon;
            return this;
        }
        
        @JsonProperty("NOTE")
        public Builder noteOccurrence(TypeOccurrenceConfig noteOccurrence) {
            this.noteOccurrence = noteOccurrence;
            return this;
        }
        
        @JsonProperty("TIP")
        public Builder tipOccurrence(TypeOccurrenceConfig tipOccurrence) {
            this.tipOccurrence = tipOccurrence;
            return this;
        }
        
        @JsonProperty("IMPORTANT")
        public Builder importantOccurrence(TypeOccurrenceConfig importantOccurrence) {
            this.importantOccurrence = importantOccurrence;
            return this;
        }
        
        @JsonProperty("WARNING")
        public Builder warningOccurrence(TypeOccurrenceConfig warningOccurrence) {
            this.warningOccurrence = warningOccurrence;
            return this;
        }
        
        @JsonProperty("CAUTION")
        public Builder cautionOccurrence(TypeOccurrenceConfig cautionOccurrence) {
            this.cautionOccurrence = cautionOccurrence;
            return this;
        }
        
        @Override
        public AdmonitionBlock build() {
            Objects.requireNonNull(severity, "severity is required");
            return new AdmonitionBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AdmonitionBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(type, that.type) &&
               Objects.equals(title, that.title) &&
               Objects.equals(content, that.content) &&
               Objects.equals(icon, that.icon) &&
               Objects.equals(noteOccurrence, that.noteOccurrence) &&
               Objects.equals(tipOccurrence, that.tipOccurrence) &&
               Objects.equals(importantOccurrence, that.importantOccurrence) &&
               Objects.equals(warningOccurrence, that.warningOccurrence) &&
               Objects.equals(cautionOccurrence, that.cautionOccurrence);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), type, title, content, icon, 
                           noteOccurrence, tipOccurrence, importantOccurrence, 
                           warningOccurrence, cautionOccurrence);
    }
}