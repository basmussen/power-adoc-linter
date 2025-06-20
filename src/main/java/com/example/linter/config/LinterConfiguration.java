package com.example.linter.config;

import java.util.Objects;

public final class LinterConfiguration {
    private final DocumentConfiguration document;

    private LinterConfiguration(Builder builder) {
        this.document = builder.document;
    }

    public DocumentConfiguration document() { return document; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private DocumentConfiguration document;

        public Builder document(DocumentConfiguration document) {
            this.document = document;
            return this;
        }

        public LinterConfiguration build() {
            // Allow empty configuration
            return new LinterConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LinterConfiguration that = (LinterConfiguration) o;
        return Objects.equals(document, that.document);
    }

    @Override
    public int hashCode() {
        return Objects.hash(document);
    }
}