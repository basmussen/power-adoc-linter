package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;
import java.util.regex.Pattern;

public final class TableBlock extends AbstractBlock {
    private final DimensionRule columns;
    private final DimensionRule rows;
    private final HeaderRule header;
    private final CaptionRule caption;
    private final FormatRule format;
    
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
    
    public DimensionRule getColumns() {
        return columns;
    }
    
    public DimensionRule getRows() {
        return rows;
    }
    
    public HeaderRule getHeader() {
        return header;
    }
    
    public CaptionRule getCaption() {
        return caption;
    }
    
    public FormatRule getFormat() {
        return format;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class DimensionRule {
        private final Integer min;
        private final Integer max;
        private final Severity severity;
        
        private DimensionRule(DimensionRuleBuilder builder) {
            this.min = builder.min;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        public Integer getMin() {
            return min;
        }
        
        public Integer getMax() {
            return max;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static DimensionRuleBuilder builder() {
            return new DimensionRuleBuilder();
        }
        
        public static class DimensionRuleBuilder {
            private Integer min;
            private Integer max;
            private Severity severity;
            
            public DimensionRuleBuilder min(Integer min) {
                this.min = min;
                return this;
            }
            
            public DimensionRuleBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public DimensionRuleBuilder severity(Severity severity) {
                this.severity = severity;
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
            return Objects.equals(min, that.min) &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(min, max, severity);
        }
    }
    
    public static class HeaderRule {
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;
        
        private HeaderRule(HeaderRuleBuilder builder) {
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
        
        public static HeaderRuleBuilder builder() {
            return new HeaderRuleBuilder();
        }
        
        public static class HeaderRuleBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            public HeaderRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public HeaderRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public HeaderRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public HeaderRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public HeaderRule build() {
                Objects.requireNonNull(severity, "severity is required for HeaderRule");
                return new HeaderRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof HeaderRule that)) return false;
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
    
    public static class CaptionRule {
        private final boolean required;
        private final Pattern pattern;
        private final Integer minLength;
        private final Integer maxLength;
        private final Severity severity;
        
        private CaptionRule(CaptionRuleBuilder builder) {
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
        
        public static CaptionRuleBuilder builder() {
            return new CaptionRuleBuilder();
        }
        
        public static class CaptionRuleBuilder {
            private boolean required;
            private Pattern pattern;
            private Integer minLength;
            private Integer maxLength;
            private Severity severity;
            
            public CaptionRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public CaptionRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public CaptionRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public CaptionRuleBuilder minLength(Integer minLength) {
                this.minLength = minLength;
                return this;
            }
            
            public CaptionRuleBuilder maxLength(Integer maxLength) {
                this.maxLength = maxLength;
                return this;
            }
            
            public CaptionRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CaptionRule build() {
                Objects.requireNonNull(severity, "severity is required for CaptionRule");
                return new CaptionRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CaptionRule that)) return false;
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
    
    public static class FormatRule {
        private final String style;
        private final Boolean borders;
        private final Severity severity;
        
        private FormatRule(FormatRuleBuilder builder) {
            this.style = builder.style;
            this.borders = builder.borders;
            this.severity = builder.severity;
        }
        
        public String getStyle() {
            return style;
        }
        
        public Boolean getBorders() {
            return borders;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static FormatRuleBuilder builder() {
            return new FormatRuleBuilder();
        }
        
        public static class FormatRuleBuilder {
            private String style;
            private Boolean borders;
            private Severity severity;
            
            public FormatRuleBuilder style(String style) {
                this.style = style;
                return this;
            }
            
            public FormatRuleBuilder borders(Boolean borders) {
                this.borders = borders;
                return this;
            }
            
            public FormatRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public FormatRule build() {
                Objects.requireNonNull(severity, "severity is required for FormatRule");
                return new FormatRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FormatRule that)) return false;
            return Objects.equals(style, that.style) &&
                   Objects.equals(borders, that.borders) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(style, borders, severity);
        }
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private DimensionRule columns;
        private DimensionRule rows;
        private HeaderRule header;
        private CaptionRule caption;
        private FormatRule format;
        
        public Builder columns(DimensionRule columns) {
            this.columns = columns;
            return this;
        }
        
        public Builder rows(DimensionRule rows) {
            this.rows = rows;
            return this;
        }
        
        public Builder header(HeaderRule header) {
            this.header = header;
            return this;
        }
        
        public Builder caption(CaptionRule caption) {
            this.caption = caption;
            return this;
        }
        
        public Builder format(FormatRule format) {
            this.format = format;
            return this;
        }
        
        @Override
        public TableBlock build() {
            Objects.requireNonNull(severity, "severity is required");
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