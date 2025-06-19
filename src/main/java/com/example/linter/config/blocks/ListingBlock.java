package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.rule.LineRule;

import java.util.*;
import java.util.regex.Pattern;

public final class ListingBlock extends AbstractBlock {
    private final LanguageRule language;
    private final LineRule lines;
    private final TitleRule title;
    private final CalloutsRule callouts;
    
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
    
    public LanguageRule getLanguage() {
        return language;
    }
    
    public LineRule getLines() {
        return lines;
    }
    
    public TitleRule getTitle() {
        return title;
    }
    
    public CalloutsRule getCallouts() {
        return callouts;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class LanguageRule {
        private final boolean required;
        private final List<String> allowed;
        private final Severity severity;
        
        private LanguageRule(LanguageRuleBuilder builder) {
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
        
        public static LanguageRuleBuilder builder() {
            return new LanguageRuleBuilder();
        }
        
        public static class LanguageRuleBuilder {
            private boolean required;
            private List<String> allowed;
            private Severity severity;
            
            public LanguageRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public LanguageRuleBuilder allowed(List<String> allowed) {
                this.allowed = allowed;
                return this;
            }
            
            public LanguageRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public LanguageRule build() {
                Objects.requireNonNull(severity, "severity is required for LanguageRule");
                return new LanguageRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof LanguageRule that)) return false;
            return required == that.required &&
                   Objects.equals(allowed, that.allowed) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(required, allowed, severity);
        }
    }
    
    public static class TitleRule {
        private final boolean required;
        private final Pattern pattern;
        private final Severity severity;
        
        private TitleRule(TitleRuleBuilder builder) {
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
        
        public static TitleRuleBuilder builder() {
            return new TitleRuleBuilder();
        }
        
        public static class TitleRuleBuilder {
            private boolean required;
            private Pattern pattern;
            private Severity severity;
            
            public TitleRuleBuilder required(boolean required) {
                this.required = required;
                return this;
            }
            
            public TitleRuleBuilder pattern(Pattern pattern) {
                this.pattern = pattern;
                return this;
            }
            
            public TitleRuleBuilder pattern(String pattern) {
                this.pattern = pattern != null ? Pattern.compile(pattern) : null;
                return this;
            }
            
            public TitleRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public TitleRule build() {
                Objects.requireNonNull(severity, "severity is required for TitleRule");
                return new TitleRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof TitleRule that)) return false;
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
    
    public static class CalloutsRule {
        private final boolean allowed;
        private final Integer max;
        private final Severity severity;
        
        private CalloutsRule(CalloutsRuleBuilder builder) {
            this.allowed = builder.allowed;
            this.max = builder.max;
            this.severity = builder.severity;
        }
        
        public boolean isAllowed() {
            return allowed;
        }
        
        public Integer getMax() {
            return max;
        }
        
        public Severity getSeverity() {
            return severity;
        }
        
        public static CalloutsRuleBuilder builder() {
            return new CalloutsRuleBuilder();
        }
        
        public static class CalloutsRuleBuilder {
            private boolean allowed;
            private Integer max;
            private Severity severity;
            
            public CalloutsRuleBuilder allowed(boolean allowed) {
                this.allowed = allowed;
                return this;
            }
            
            public CalloutsRuleBuilder max(Integer max) {
                this.max = max;
                return this;
            }
            
            public CalloutsRuleBuilder severity(Severity severity) {
                this.severity = severity;
                return this;
            }
            
            public CalloutsRule build() {
                Objects.requireNonNull(severity, "severity is required for CalloutsRule");
                return new CalloutsRule(this);
            }
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CalloutsRule that)) return false;
            return allowed == that.allowed &&
                   Objects.equals(max, that.max) &&
                   severity == that.severity;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(allowed, max, severity);
        }
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private LanguageRule language;
        private LineRule lines;
        private TitleRule title;
        private CalloutsRule callouts;
        
        public Builder language(LanguageRule language) {
            this.language = language;
            return this;
        }
        
        public Builder lines(LineRule lines) {
            this.lines = lines;
            return this;
        }
        
        public Builder title(TitleRule title) {
            this.title = title;
            return this;
        }
        
        public Builder callouts(CalloutsRule callouts) {
            this.callouts = callouts;
            return this;
        }
        
        @Override
        public ListingBlock build() {
            Objects.requireNonNull(severity, "severity is required");
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