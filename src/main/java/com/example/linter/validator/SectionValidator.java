package com.example.linter.validator;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.DocumentConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.SectionConfig;
import com.example.linter.config.rule.TitleConfig;

public final class SectionValidator {
    private final DocumentConfiguration configuration;
    private final Map<String, Integer> sectionOccurrences;
    private final List<SectionConfig> rootSections;

    private SectionValidator(Builder builder) {
        this.configuration = Objects.requireNonNull(builder.configuration, "configuration must not be null");
        this.sectionOccurrences = new HashMap<>();
        this.rootSections = configuration.sections() != null ? configuration.sections() : Collections.emptyList();
    }

    public ValidationResult validate(Document document) {
        long startTime = System.currentTimeMillis();
        ValidationResult.Builder resultBuilder = ValidationResult.builder().startTime(startTime);
        
        String filename = extractFilename(document);
        
        List<StructuralNode> sections = document.getBlocks().stream()
            .filter(block -> block instanceof Section)
            .collect(Collectors.toList());
        
        validateRootSections(sections, filename, resultBuilder);
        
        validateMinMaxOccurrences(filename, resultBuilder);
        
        validateSectionOrder(sections, filename, resultBuilder);
        
        return resultBuilder.complete().build();
    }

    private void validateRootSections(List<StructuralNode> sections, String filename, ValidationResult.Builder resultBuilder) {
        for (StructuralNode node : sections) {
            if (node instanceof Section) {
                Section section = (Section) node;
                validateSection(section, rootSections, filename, resultBuilder, null);
            }
        }
    }

    private void validateSection(Section section, List<SectionConfig> allowedConfigs, 
                                String filename, ValidationResult.Builder resultBuilder, 
                                SectionConfig parentConfig) {
        
        int level = section.getLevel();
        String title = section.getTitle();
        
        SectionConfig matchingConfig = findMatchingConfig(section, allowedConfigs);
        
        if (matchingConfig == null && !allowedConfigs.isEmpty()) {
            SourceLocation location = createLocation(filename, section);
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.unexpected")
                .location(location)
                .message("Unexpected section at level " + level + ": '" + title + "'")
                .actualValue(title)
                .expectedValue("One of configured sections")
                .build();
            resultBuilder.addMessage(message);
            return;
        }
        
        if (matchingConfig != null) {
            trackOccurrence(matchingConfig);
            
            validateTitle(section, matchingConfig.title(), filename, resultBuilder);
            
            validateLevel(section, matchingConfig, filename, resultBuilder);
            
            List<StructuralNode> subsections = section.getBlocks().stream()
                .filter(block -> block instanceof Section)
                .collect(Collectors.toList());
            
            for (StructuralNode subsection : subsections) {
                validateSection((Section) subsection, matchingConfig.subsections(), 
                               filename, resultBuilder, matchingConfig);
            }
        }
    }

    private void validateTitle(Section section, TitleConfig titleConfig, 
                              String filename, ValidationResult.Builder resultBuilder) {
        if (titleConfig == null) {
            return;
        }
        
        String title = section.getTitle();
        SourceLocation location = createLocation(filename, section);
        
        if (titleConfig.pattern() != null) {
            Pattern pattern = Pattern.compile(titleConfig.pattern());
            if (!pattern.matcher(title).matches()) {
                ValidationMessage message = ValidationMessage.builder()
                    .severity(titleConfig.severity())
                    .ruleId("section.title.pattern")
                    .location(location)
                    .message("Section title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + titleConfig.pattern())
                    .build();
                resultBuilder.addMessage(message);
            }
        }
        
        if (titleConfig.exactMatch() != null && !title.equals(titleConfig.exactMatch())) {
            ValidationMessage message = ValidationMessage.builder()
                .severity(titleConfig.severity())
                .ruleId("section.title.exact")
                .location(location)
                .message("Section title does not match expected value")
                .actualValue(title)
                .expectedValue(titleConfig.exactMatch())
                .build();
            resultBuilder.addMessage(message);
        }
    }

    private void validateLevel(Section section, SectionConfig config, 
                              String filename, ValidationResult.Builder resultBuilder) {
        int actualLevel = section.getLevel();
        int expectedLevel = config.level();
        
        if (actualLevel != expectedLevel) {
            SourceLocation location = createLocation(filename, section);
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.level")
                .location(location)
                .message("Section level mismatch")
                .actualValue(String.valueOf(actualLevel))
                .expectedValue(String.valueOf(expectedLevel))
                .build();
            resultBuilder.addMessage(message);
        }
    }

    private void validateMinMaxOccurrences(String filename, ValidationResult.Builder resultBuilder) {
        for (SectionConfig config : rootSections) {
            validateOccurrenceForConfig(config, filename, resultBuilder);
        }
    }

    private void validateOccurrenceForConfig(SectionConfig config, String filename, 
                                            ValidationResult.Builder resultBuilder) {
        String key = createOccurrenceKey(config);
        int occurrences = sectionOccurrences.getOrDefault(key, 0);
        
        if (occurrences < config.min()) {
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.min-occurrences")
                .location(location)
                .message("Too few occurrences of section: " + config.name())
                .actualValue(String.valueOf(occurrences))
                .expectedValue("At least " + config.min())
                .build();
            resultBuilder.addMessage(message);
        }
        
        if (occurrences > config.max()) {
            SourceLocation location = SourceLocation.builder()
                .filename(filename)
                .line(1)
                .build();
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("section.max-occurrences")
                .location(location)
                .message("Too many occurrences of section: " + config.name())
                .actualValue(String.valueOf(occurrences))
                .expectedValue("At most " + config.max())
                .build();
            resultBuilder.addMessage(message);
        }
        
        for (SectionConfig subsection : config.subsections()) {
            validateOccurrenceForConfig(subsection, filename, resultBuilder);
        }
    }

    private void validateSectionOrder(List<StructuralNode> sections, String filename, 
                                     ValidationResult.Builder resultBuilder) {
        List<SectionConfig> orderedConfigs = rootSections.stream()
            .filter(config -> config.order() != null)
            .sorted(Comparator.comparing(SectionConfig::order))
            .collect(Collectors.toList());
        
        if (orderedConfigs.isEmpty()) {
            return;
        }
        
        Map<String, Integer> actualOrder = new HashMap<>();
        for (int i = 0; i < sections.size(); i++) {
            if (sections.get(i) instanceof Section) {
                Section section = (Section) sections.get(i);
                SectionConfig config = findMatchingConfig(section, rootSections);
                if (config != null && config.order() != null) {
                    actualOrder.put(config.name(), i);
                }
            }
        }
        
        for (int i = 0; i < orderedConfigs.size() - 1; i++) {
            SectionConfig current = orderedConfigs.get(i);
            SectionConfig next = orderedConfigs.get(i + 1);
            
            Integer currentPos = actualOrder.get(current.name());
            Integer nextPos = actualOrder.get(next.name());
            
            if (currentPos != null && nextPos != null && currentPos > nextPos) {
                SourceLocation location = SourceLocation.builder()
                    .filename(filename)
                    .line(1)
                    .build();
                
                ValidationMessage message = ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("section.order")
                    .location(location)
                    .message("Section order violation")
                    .actualValue(current.name() + " appears after " + next.name())
                    .expectedValue(current.name() + " should appear before " + next.name())
                    .build();
                resultBuilder.addMessage(message);
            }
        }
    }

    private SectionConfig findMatchingConfig(Section section, List<SectionConfig> configs) {
        String title = section.getTitle();
        int level = section.getLevel();
        
        for (SectionConfig config : configs) {
            if (config.level() != level) {
                continue;
            }
            
            if (config.title() != null) {
                if (config.title().exactMatch() != null && 
                    config.title().exactMatch().equals(title)) {
                    return config;
                }
                
                if (config.title().pattern() != null) {
                    Pattern pattern = Pattern.compile(config.title().pattern());
                    if (pattern.matcher(title).matches()) {
                        return config;
                    }
                }
            } else if (config.name() != null) {
                return config;
            }
        }
        
        return null;
    }

    private void trackOccurrence(SectionConfig config) {
        String key = createOccurrenceKey(config);
        sectionOccurrences.merge(key, 1, Integer::sum);
    }

    private String createOccurrenceKey(SectionConfig config) {
        return config.name() + "_" + config.level();
    }

    private String extractFilename(Document document) {
        Map<String, Object> attrs = document.getAttributes();
        if (attrs.containsKey("docfile")) {
            return attrs.get("docfile").toString();
        }
        return "unknown";
    }

    private SourceLocation createLocation(String filename, Section section) {
        return SourceLocation.builder()
            .filename(filename)
            .line(section.getSourceLocation() != null ? section.getSourceLocation().getLineNumber() : 1)
            .build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder fromConfiguration(DocumentConfiguration configuration) {
        return builder().configuration(configuration);
    }

    public static final class Builder {
        private DocumentConfiguration configuration;

        private Builder() {
        }

        public Builder configuration(DocumentConfiguration configuration) {
            this.configuration = configuration;
            return this;
        }

        public SectionValidator build() {
            return new SectionValidator(this);
        }
    }
}