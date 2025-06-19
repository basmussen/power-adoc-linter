package com.example.linter.config;

import com.example.linter.rule.AttributeRule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class MetadataConfiguration {
    private final List<AttributeRule> attributes;

    private MetadataConfiguration(Builder builder) {
        this.attributes = Collections.unmodifiableList(new ArrayList<>(builder.attributes));
    }

    public List<AttributeRule> attributes() { 
        return attributes; 
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<AttributeRule> attributes = new ArrayList<>();

        public Builder attributes(List<AttributeRule> attributes) {
            this.attributes = attributes != null ? new ArrayList<>(attributes) : new ArrayList<>();
            return this;
        }

        public Builder addAttribute(AttributeRule attribute) {
            this.attributes.add(attribute);
            return this;
        }

        public MetadataConfiguration build() {
            return new MetadataConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataConfiguration that = (MetadataConfiguration) o;
        return Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributes);
    }
}