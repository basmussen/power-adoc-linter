package com.example.linter.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class DocumentConfiguration {
    private final MetadataConfiguration metadata;
    private final List<SectionRule> sections;

    private DocumentConfiguration(Builder builder) {
        this.metadata = builder.metadata;
        this.sections = Collections.unmodifiableList(new ArrayList<>(builder.sections));
    }

    public MetadataConfiguration metadata() { return metadata; }
    public List<SectionRule> sections() { return sections; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private MetadataConfiguration metadata;
        private List<SectionRule> sections = new ArrayList<>();

        public Builder metadata(MetadataConfiguration metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder sections(List<SectionRule> sections) {
            this.sections = sections != null ? new ArrayList<>(sections) : new ArrayList<>();
            return this;
        }

        public Builder addSection(SectionRule section) {
            this.sections.add(section);
            return this;
        }

        public DocumentConfiguration build() {
            return new DocumentConfiguration(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentConfiguration that = (DocumentConfiguration) o;
        return Objects.equals(metadata, that.metadata) &&
               Objects.equals(sections, that.sections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(metadata, sections);
    }
}