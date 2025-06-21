package com.example.linter.config.blocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.LineConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = ListingBlock.Builder.class)
public final class ListingBlock extends AbstractBlock {
    private final LanguageConfig language;
    private final LineConfig lines;
    private final TitleConfig title;
    private final CalloutsConfig callouts;
    
    private ListingBlock(Builder builder) {
        super(builder);
        this.language = builder.language;
        this.lines = builder.lines;
        this.title = builder.title;
        this.callouts = builder.callouts;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.LISTING;
    }
    
    @JsonProperty("language")
    public LanguageConfig getLanguage() {
        return language;
    }
    
    @JsonProperty("lines")
    public LineConfig getLines() {
        return lines;
    }
    
    @JsonProperty("title")
    public TitleConfig getTitle() {
        return title;
    }
    
    @JsonProperty("callouts")
    public CalloutsConfig getCallouts() {
        return callouts;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = LanguageConfig.LanguageConfigBuilder.class)
    public static class LanguageConfig {
        private final boolean required;
        private final List<String> allowed;
        private final Severity severity;
        
        private LanguageConfig(LanguageConfigBuilder builder) {
            this.required = builder.required;
            this.allowed = builder.allowed != null ? 
                Collections.unmodifiableList(new ArrayList<>(builder.allowed)) : 
                Collections.emptyList();
            this.severity = builder.severity;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("allowed")
        public List<String> getAllowed() {
            return allowed;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static LanguageConfigBuilder builder() {
            return new LanguageConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class LanguageConfigBuilder {
            private boolean required;
            private List<String> allowed;
            private Severity severity;
            
            @JsonProperty("required")
            public LanguageConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            @JsonProperty("allowed")
            public LanguageConfigBuilder allowed(List<String> allowed) {
                this.allowed = allowed;
                return this;
            }
            
            @JsonProperty("severity")
            public LanguageConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public LanguageConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new LanguageConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LanguageConfig that)) return false;
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
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;
        
        private TitleConfig(TitleConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.severity = builder.severity;
        }
        
        @JsonProperty("required")
        public boolean isRequired() {
            return required;
        }
        
        @JsonProperty("pattern")
        public Pattern getPattern() {
            return pattern;
        }
        
        @JsonProperty("severity")
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
            private Severity severity;
            
            @JsonProperty("required")
            public TitleConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public TitleConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public TitleConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public TitleConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TitleConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
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
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, pattern == null ? null : pattern.pattern(), severity);
        }
    }
    
    @JsonDeserialize(builder = CalloutsConfig.CalloutsConfigBuilder.class)
    public static class CalloutsConfig {
        private final boolean allowed;
        private final Integer max;
        private final Severity severity;
        
        private CalloutsConfig(CalloutsConfigBuilder builder) {
            this.allowed = builder.allowed;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        @JsonProperty("allowed")
        public boolean isAllowed() {
            return allowed;
        }
        
        @JsonProperty("max")
        public Integer getMax() {
            return max;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static CalloutsConfigBuilder builder() {
            return new CalloutsConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class CalloutsConfigBuilder {
            private boolean allowed;
            private Integer max;
            private Severity severity;
            
            @JsonProperty("allowed")
            public CalloutsConfigBuilder allowed(boolean allowed) {
                this.allowed = allowed;
                return this;
            }
            
            @JsonProperty("max")
            public CalloutsConfigBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            @JsonProperty("severity")
            public CalloutsConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CalloutsConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new CalloutsConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CalloutsConfig that)) return false;
            return allowed == that.allowed &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(allowed, max, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private LanguageConfig language;
        private LineConfig lines;
        private TitleConfig title;
        private CalloutsConfig callouts;
        
        @JsonProperty("language")
        public Builder language(LanguageConfig language) {
            this.language = language;
            return this;
        }
        
        @JsonProperty("lines")
        public Builder lines(LineConfig lines) {
            this.lines = lines;
            return this;
        }
        
        @JsonProperty("title")
        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }
        
        @JsonProperty("callouts")
        public Builder callouts(CalloutsConfig callouts) {
            this.callouts = callouts;
            return this;
        }
        
        @Override
        public ListingBlock build() {
            if (severity == null) {
                severity = Severity.WARN;
            }
            return new ListingBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListingBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(language, that.language) &&
               Objects.equals(lines, that.lines) &&
               Objects.equals(title, that.title) &&
               Objects.equals(callouts, that.callouts);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), language, lines, title, callouts);
    }
}