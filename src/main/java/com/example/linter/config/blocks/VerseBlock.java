package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;

import java.util.Objects;
import java.util.regex.Pattern;

public final class VerseBlock extends AbstractBlock {
    private final String author;
    private final Integer authorMinLength;
    private final Integer authorMaxLength;
    private final Pattern authorPattern;
    private final boolean authorRequired;
    
    private final String attribution;
    private final Integer attributionMinLength;
    private final Integer attributionMaxLength;
    private final Pattern attributionPattern;
    private final boolean attributionRequired;
    
    private final Integer contentMinLength;
    private final Integer contentMaxLength;
    private final Pattern contentPattern;
    private final boolean contentRequired;
    
    private VerseBlock(Builder builder) {
        super(builder);
        this.author = builder.author;
        this.authorMinLength = builder.authorMinLength;
        this.authorMaxLength = builder.authorMaxLength;
        this.authorPattern = builder.authorPattern;
        this.authorRequired = builder.authorRequired;
        
        this.attribution = builder.attribution;
        this.attributionMinLength = builder.attributionMinLength;
        this.attributionMaxLength = builder.attributionMaxLength;
        this.attributionPattern = builder.attributionPattern;
        this.attributionRequired = builder.attributionRequired;
        
        this.contentMinLength = builder.contentMinLength;
        this.contentMaxLength = builder.contentMaxLength;
        this.contentPattern = builder.contentPattern;
        this.contentRequired = builder.contentRequired;
    }
    
    @Override
    public BlockType getType() {
        return BlockType.VERSE;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public Integer getAuthorMinLength() {
        return authorMinLength;
    }
    
    public Integer getAuthorMaxLength() {
        return authorMaxLength;
    }
    
    public Pattern getAuthorPattern() {
        return authorPattern;
    }
    
    public boolean isAuthorRequired() {
        return authorRequired;
    }
    
    public String getAttribution() {
        return attribution;
    }
    
    public Integer getAttributionMinLength() {
        return attributionMinLength;
    }
    
    public Integer getAttributionMaxLength() {
        return attributionMaxLength;
    }
    
    public Pattern getAttributionPattern() {
        return attributionPattern;
    }
    
    public boolean isAttributionRequired() {
        return attributionRequired;
    }
    
    public Integer getContentMinLength() {
        return contentMinLength;
    }
    
    public Integer getContentMaxLength() {
        return contentMaxLength;
    }
    
    public Pattern getContentPattern() {
        return contentPattern;
    }
    
    public boolean isContentRequired() {
        return contentRequired;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder extends AbstractBuilder<Builder> {
        private String author;
        private Integer authorMinLength;
        private Integer authorMaxLength;
        private Pattern authorPattern;
        private boolean authorRequired;
        
        private String attribution;
        private Integer attributionMinLength;
        private Integer attributionMaxLength;
        private Pattern attributionPattern;
        private boolean attributionRequired;
        
        private Integer contentMinLength;
        private Integer contentMaxLength;
        private Pattern contentPattern;
        private boolean contentRequired;
        
        public Builder author(String author) {
            this.author = author;
            return this;
        }
        
        public Builder authorMinLength(Integer authorMinLength) {
            this.authorMinLength = authorMinLength;
            return this;
        }
        
        public Builder authorMaxLength(Integer authorMaxLength) {
            this.authorMaxLength = authorMaxLength;
            return this;
        }
        
        public Builder authorPattern(Pattern authorPattern) {
            this.authorPattern = authorPattern;
            return this;
        }
        
        public Builder authorPattern(String authorPattern) {
            this.authorPattern = authorPattern != null ? Pattern.compile(authorPattern) : null;
            return this;
        }
        
        public Builder authorRequired(boolean authorRequired) {
            this.authorRequired = authorRequired;
            return this;
        }
        
        public Builder attribution(String attribution) {
            this.attribution = attribution;
            return this;
        }
        
        public Builder attributionMinLength(Integer attributionMinLength) {
            this.attributionMinLength = attributionMinLength;
            return this;
        }
        
        public Builder attributionMaxLength(Integer attributionMaxLength) {
            this.attributionMaxLength = attributionMaxLength;
            return this;
        }
        
        public Builder attributionPattern(Pattern attributionPattern) {
            this.attributionPattern = attributionPattern;
            return this;
        }
        
        public Builder attributionPattern(String attributionPattern) {
            this.attributionPattern = attributionPattern != null ? Pattern.compile(attributionPattern) : null;
            return this;
        }
        
        public Builder attributionRequired(boolean attributionRequired) {
            this.attributionRequired = attributionRequired;
            return this;
        }
        
        public Builder contentMinLength(Integer contentMinLength) {
            this.contentMinLength = contentMinLength;
            return this;
        }
        
        public Builder contentMaxLength(Integer contentMaxLength) {
            this.contentMaxLength = contentMaxLength;
            return this;
        }
        
        public Builder contentPattern(Pattern contentPattern) {
            this.contentPattern = contentPattern;
            return this;
        }
        
        public Builder contentPattern(String contentPattern) {
            this.contentPattern = contentPattern != null ? Pattern.compile(contentPattern) : null;
            return this;
        }
        
        public Builder contentRequired(boolean contentRequired) {
            this.contentRequired = contentRequired;
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
        return authorRequired == that.authorRequired && 
               attributionRequired == that.attributionRequired && 
               contentRequired == that.contentRequired &&
               Objects.equals(author, that.author) && 
               Objects.equals(authorMinLength, that.authorMinLength) && 
               Objects.equals(authorMaxLength, that.authorMaxLength) && 
               Objects.equals(authorPattern == null ? null : authorPattern.pattern(), 
                             that.authorPattern == null ? null : that.authorPattern.pattern()) &&
               Objects.equals(attribution, that.attribution) && 
               Objects.equals(attributionMinLength, that.attributionMinLength) && 
               Objects.equals(attributionMaxLength, that.attributionMaxLength) && 
               Objects.equals(attributionPattern == null ? null : attributionPattern.pattern(), 
                             that.attributionPattern == null ? null : that.attributionPattern.pattern()) &&
               Objects.equals(contentMinLength, that.contentMinLength) && 
               Objects.equals(contentMaxLength, that.contentMaxLength) && 
               Objects.equals(contentPattern == null ? null : contentPattern.pattern(), 
                             that.contentPattern == null ? null : that.contentPattern.pattern());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), author, authorMinLength, authorMaxLength, 
                           authorPattern == null ? null : authorPattern.pattern(), authorRequired,
                           attribution, attributionMinLength, attributionMaxLength, 
                           attributionPattern == null ? null : attributionPattern.pattern(), attributionRequired,
                           contentMinLength, contentMaxLength, 
                           contentPattern == null ? null : contentPattern.pattern(), contentRequired);
    }
}