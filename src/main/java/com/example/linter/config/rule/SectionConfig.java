package com.example.linter.config.rule;

import com.example.linter.config.blocks.AbstractBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SectionConfig {
    private final String name;
    private final Integer order;
    private final int level;
    private final int min;
    private final int max;
    private final TitleConfig title;
    private final List<AbstractBlock> allowedBlocks;
    private final List<SectionConfig> subsections;

    private SectionConfig(Builder builder) {
        this.name = builder.name;
        this.order = builder.order;
        this.level = builder.level;
        this.min = builder.min;
        this.max = builder.max;
        this.title = builder.title;
        this.allowedBlocks = Collections.unmodifiableList(new ArrayList<>(builder.allowedBlocks));
        this.subsections = Collections.unmodifiableList(new ArrayList<>(builder.subsections));
    }

    public String name() { return name; }
    public Integer order() { return order; }
    public int level() { return level; }
    public int min() { return min; }
    public int max() { return max; }
    public TitleConfig title() { return title; }
    public List<AbstractBlock> allowedBlocks() { return allowedBlocks; }
    public List<SectionConfig> subsections() { return subsections; }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String name;
        private Integer order;
        private int level;
        private int min = 0;
        private int max = Integer.MAX_VALUE;
        private TitleConfig title;
        private List<AbstractBlock> allowedBlocks = new ArrayList<>();
        private List<SectionConfig> subsections = new ArrayList<>();

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder order(Integer order) {
            this.order = order;
            return this;
        }

        public Builder level(int level) {
            this.level = level;
            return this;
        }

        public Builder min(int min) {
            this.min = min;
            return this;
        }

        public Builder max(int max) {
            this.max = max;
            return this;
        }

        public Builder title(TitleConfig title) {
            this.title = title;
            return this;
        }

        public Builder allowedBlocks(List<AbstractBlock> allowedBlocks) {
            this.allowedBlocks = allowedBlocks != null ? new ArrayList<>(allowedBlocks) : new ArrayList<>();
            return this;
        }

        public Builder addAllowedBlock(AbstractBlock block) {
            this.allowedBlocks.add(block);
            return this;
        }

        public Builder subsections(List<SectionConfig> subsections) {
            this.subsections = subsections != null ? new ArrayList<>(subsections) : new ArrayList<>();
            return this;
        }

        public Builder addSubsection(SectionConfig subsection) {
            this.subsections.add(subsection);
            return this;
        }

        public SectionConfig build() {
            return new SectionConfig(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectionConfig that = (SectionConfig) o;
        return level == that.level &&
               min == that.min &&
               max == that.max &&
               Objects.equals(name, that.name) &&
               Objects.equals(order, that.order) &&
               Objects.equals(title, that.title) &&
               Objects.equals(allowedBlocks, that.allowedBlocks) &&
               Objects.equals(subsections, that.subsections);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, level, min, max, title, allowedBlocks, subsections);
    }
}