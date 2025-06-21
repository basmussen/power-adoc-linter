package com.example.linter.config.blocks;

import java.util.Objects;
import java.util.regex.Pattern;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

@JsonDeserialize(builder = TableBlock.Builder.class)
public final class TableBlock extends AbstractBlock {
    private final DimensionConfig columns;
    private final DimensionConfig rows;
    private final HeaderConfig header;
    private final CaptionConfig caption;
    private final FormatConfig format;
    
    private TableBlock(Builder builder) {
        super(builder);
        this.columns = builder.columns;
        this.rows = builder.rows;
        this.header = builder.header;
        this.caption = builder.caption;
        this.format = builder.format;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.TABLE;
    }
    
    @JsonProperty("columns")
    public DimensionConfig getColumns() {
        return columns;
    }
    
    @JsonProperty("rows")
    public DimensionConfig getRows() {
        return rows;
    }
    
    @JsonProperty("header")
    public HeaderConfig getHeader() {
        return header;
    }
    
    @JsonProperty("caption")
    public CaptionConfig getCaption() {
        return caption;
    }
    
    @JsonProperty("format")
    public FormatConfig getFormat() {
        return format;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    @JsonDeserialize(builder = DimensionConfig.DimensionConfigBuilder.class)
    public static class DimensionConfig {
        private final Integer min;
        private final Integer max;
        private final Severity severity;
        
        private DimensionConfig(DimensionConfigBuilder builder) {
            this.min = builder.min;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        @JsonProperty("min")
        public Integer getMin() {
            return min;
        }
        
        @JsonProperty("max")
        public Integer getMax() {
            return max;
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
            private Integer min;
            private Integer max;
            private Severity severity;
            
            @JsonProperty("min")
            public DimensionConfigBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            @JsonProperty("max")
            public DimensionConfigBuilder max(Integer max) {
                this.max = max;
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
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }
    
    @JsonDeserialize(builder = HeaderConfig.HeaderConfigBuilder.class)
    public static class HeaderConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;
        
        private HeaderConfig(HeaderConfigBuilder builder) {
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
        
        public static HeaderConfigBuilder builder() {
            return new HeaderConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class HeaderConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            @JsonProperty("required")
            public HeaderConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public HeaderConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public HeaderConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            @JsonProperty("severity")
            public HeaderConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public HeaderConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new HeaderConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderConfig that)) return false;
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
    
    @JsonDeserialize(builder = CaptionConfig.CaptionConfigBuilder.class)
    public static class CaptionConfig {
        private final boolean required;
        private final Pattern pattern;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;
        
        private CaptionConfig(CaptionConfigBuilder builder) {
            this.required = builder.required;
            this.pattern = builder.pattern;
            this.minLength = builder.minLength;
            this.maxLength = builder.maxLength;
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
        
        public static CaptionConfigBuilder builder() {
            return new CaptionConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class CaptionConfigBuilder {
            private boolean required;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            @JsonProperty("required")
            public CaptionConfigBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public CaptionConfigBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            @JsonProperty("pattern")
            public CaptionConfigBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
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
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new CaptionConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CaptionConfig that)) return false;
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
    
    @JsonDeserialize(builder = FormatConfig.FormatConfigBuilder.class)
    public static class FormatConfig {
        private final String style;
        private final Boolean borders;
        private final Severity severity;
        
        private FormatConfig(FormatConfigBuilder builder) {
            this.style = builder.style;
            this.borders = builder.borders;
            this.severity = builder.severity;
        }
        
        @JsonProperty("style")
        public String getStyle() {
            return style;
        }
        
        @JsonProperty("borders")
        public Boolean getBorders() {
            return borders;
        }
        
        @JsonProperty("severity")
        public Severity getSeverity() {
            return severity;
        }
        
        public static FormatConfigBuilder builder() {
            return new FormatConfigBuilder();
        }
        
        @JsonPOJOBuilder(withPrefix = "")
        public static class FormatConfigBuilder {
            private String style;
            private Boolean borders;
            private Severity severity;
            
            @JsonProperty("style")
            public FormatConfigBuilder style(String style) {
                this.style = style;
                return this;
            }
            
            @JsonProperty("borders")
            public FormatConfigBuilder borders(Boolean borders) {
                this.borders = borders;
                return this;
            }
            
            @JsonProperty("severity")
            public FormatConfigBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public FormatConfig build() {
                if (severity == null) {
                    severity = Severity.WARN;
                }
                return new FormatConfig(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FormatConfig that)) return false;
            return Objects.equals(style, that.style) &&
                   Objects.equals(borders, that.borders) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(style, borders, severity);
        }
    }
    
    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder extends AbstractBuilder<Builder> {
        private DimensionConfig columns;
        private DimensionConfig rows;
        private HeaderConfig header;
        private CaptionConfig caption;
        private FormatConfig format;
        
        @JsonProperty("columns")
        public Builder columns(DimensionConfig columns) {
            this.columns = columns;
            return this;
        }
        
        @JsonProperty("rows")
        public Builder rows(DimensionConfig rows) {
            this.rows = rows;
            return this;
        }
        
        @JsonProperty("header")
        public Builder header(HeaderConfig header) {
            this.header = header;
            return this;
        }
        
        @JsonProperty("caption")
        public Builder caption(CaptionConfig caption) {
            this.caption = caption;
            return this;
        }
        
        @JsonProperty("format")
        public Builder format(FormatConfig format) {
            this.format = format;
            return this;
        }
        
        @Override
        public TableBlock build() {
            if (severity == null) {
                severity = Severity.WARN;
            }
            return new TableBlock(this);
        }
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TableBlock that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(columns, that.columns) &&
               Objects.equals(rows, that.rows) &&
               Objects.equals(header, that.header) &&
               Objects.equals(caption, that.caption) &&
               Objects.equals(format, that.format);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), columns, rows, header, caption, format);
    }
}