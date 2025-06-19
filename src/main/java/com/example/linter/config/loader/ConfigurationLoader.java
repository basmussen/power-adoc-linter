package com.example.linter.config.loader;

import com.example.linter.config.*;
import com.example.linter.config.blocks.*;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.ScalarNode;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ConfigurationLoader {
    
    private final Yaml yaml;
    
    public ConfigurationLoader() {
        LoaderOptions loaderOptions = new LoaderOptions();
        CustomConstructor constructor = new CustomConstructor(loaderOptions);
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        
        this.yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }
    
    public LinterConfiguration loadConfiguration(Path configPath) throws IOException {
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            return loadConfiguration(inputStream);
        }
    }
    
    public LinterConfiguration loadConfiguration(InputStream inputStream) {
        try {
            Map<String, Object> rawConfig = yaml.load(inputStream);
            if (rawConfig == null || rawConfig.isEmpty()) {
                throw new ConfigurationException("Configuration file is empty");
            }
            return parseConfiguration(rawConfig);
        } catch (Exception e) {
            if (e instanceof ConfigurationException) {
                throw e;
            }
            throw new ConfigurationException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
    
    private LinterConfiguration parseConfiguration(Map<String, Object> raw) {
        if (!raw.containsKey("document")) {
            throw new ConfigurationException("Missing required 'document' section in configuration");
        }
        Map<String, Object> documentRaw = (Map<String, Object>) raw.get("document");
        DocumentConfiguration document = parseDocumentConfiguration(documentRaw);
        return LinterConfiguration.builder()
            .document(document)
            .build();
    }
    
    private DocumentConfiguration parseDocumentConfiguration(Map<String, Object> raw) {
        Map<String, Object> metadataRaw = (Map<String, Object>) raw.get("metadata");
        MetadataConfiguration metadata = parseMetadataConfiguration(metadataRaw);
        
        List<Map<String, Object>> sectionsRaw = (List<Map<String, Object>>) raw.get("sections");
        List<SectionRule> sections = parseSectionRules(sectionsRaw);
        
        return DocumentConfiguration.builder()
            .metadata(metadata)
            .sections(sections)
            .build();
    }
    
    private MetadataConfiguration parseMetadataConfiguration(Map<String, Object> raw) {
        List<Map<String, Object>> attributesRaw = (List<Map<String, Object>>) raw.get("attributes");
        List<AttributeRule> attributes = new ArrayList<>();
        
        for (Map<String, Object> attrRaw : attributesRaw) {
            attributes.add(parseAttributeRule(attrRaw));
        }
        
        return MetadataConfiguration.builder()
            .attributes(attributes)
            .build();
    }
    
    private AttributeRule parseAttributeRule(Map<String, Object> raw) {
        return AttributeRule.builder()
            .name((String) raw.get("name"))
            .order((Integer) raw.get("order"))
            .required(Boolean.TRUE.equals(raw.get("required")))
            .minLength((Integer) raw.get("minLength"))
            .maxLength((Integer) raw.get("maxLength"))
            .pattern((String) raw.get("pattern"))
            .severity(parseSeverity((String) raw.get("severity")))
            .build();
    }
    
    private List<SectionRule> parseSectionRules(List<Map<String, Object>> sectionsRaw) {
        List<SectionRule> sections = new ArrayList<>();
        if (sectionsRaw != null) {
            for (Map<String, Object> sectionRaw : sectionsRaw) {
                sections.add(parseSectionRule(sectionRaw));
            }
        }
        return sections;
    }
    
    private SectionRule parseSectionRule(Map<String, Object> raw) {
        TitleRule title = null;
        if (raw.containsKey("title")) {
            Map<String, Object> titleRaw = (Map<String, Object>) raw.get("title");
            title = TitleRule.builder()
                .pattern((String) titleRaw.get("pattern"))
                .build();
        }
        
        List<AbstractBlock> allowedBlocks = new ArrayList<>();
        if (raw.containsKey("allowedBlocks")) {
            List<Map<String, Object>> blocksRaw = (List<Map<String, Object>>) raw.get("allowedBlocks");
            for (Map<String, Object> blockRaw : blocksRaw) {
                allowedBlocks.add(parseBlock(blockRaw));
            }
        }
        
        List<SectionRule> subsections = new ArrayList<>();
        if (raw.containsKey("subsections")) {
            subsections = parseSectionRules((List<Map<String, Object>>) raw.get("subsections"));
        }
        
        SectionRule.Builder builder = SectionRule.builder()
            .name((String) raw.get("name"))
            .order((Integer) raw.get("order"))
            .level((Integer) raw.get("level"))
            .title(title)
            .allowedBlocks(allowedBlocks)
            .subsections(subsections);
            
        if (raw.get("min") != null) {
            builder.min((Integer) raw.get("min"));
        }
        if (raw.get("max") != null) {
            builder.max((Integer) raw.get("max"));
        }
        
        return builder.build();
    }
    
    private AbstractBlock parseBlock(Map<String, Object> raw) {
        // The YAML structure has block type as key (e.g., "paragraph:", "listing:")
        Map.Entry<String, Object> entry = raw.entrySet().iterator().next();
        String blockTypeStr = entry.getKey();
        Map<String, Object> blockData = (Map<String, Object>) entry.getValue();
        
        BlockType type = parseBlockType(blockTypeStr);
        String name = (String) blockData.get("name");
        Severity severity = parseSeverity((String) blockData.get("severity"));
        
        // Default severity if not specified
        if (severity == null) {
            severity = Severity.WARN;
        }
        
        OccurrenceRule occurrence = null;
        if (blockData.containsKey("occurrence")) {
            Map<String, Object> occRaw = (Map<String, Object>) blockData.get("occurrence");
            OccurrenceRule.Builder occBuilder = OccurrenceRule.builder()
                .order((Integer) occRaw.get("order"));
            
            if (occRaw.get("min") != null) {
                occBuilder.min((Integer) occRaw.get("min"));
            }
            if (occRaw.get("max") != null) {
                occBuilder.max((Integer) occRaw.get("max"));
            }
            if (occRaw.containsKey("severity")) {
                occBuilder.severity(parseSeverity((String) occRaw.get("severity")));
            }
            
            occurrence = occBuilder.build();
        }
        
        LineRule lines = null;
        if (blockData.containsKey("lines")) {
            Map<String, Object> linesRaw = (Map<String, Object>) blockData.get("lines");
            LineRule.Builder lineBuilder = LineRule.builder()
                .min((Integer) linesRaw.get("min"))
                .max((Integer) linesRaw.get("max"));
            
            if (linesRaw.containsKey("severity")) {
                lineBuilder.severity(parseSeverity((String) linesRaw.get("severity")));
            }
            
            lines = lineBuilder.build();
        }
        
        // Handle direct min/max when no occurrence block
        if (occurrence == null && (blockData.containsKey("min") || blockData.containsKey("max"))) {
            OccurrenceRule.Builder occBuilder = OccurrenceRule.builder();
            
            if (blockData.get("min") != null) {
                occBuilder.min((Integer) blockData.get("min"));
            }
            if (blockData.get("max") != null) {
                occBuilder.max((Integer) blockData.get("max"));
            }
            
            occurrence = occBuilder.build();
        }
        
        // Create specific block instance based on type
        return switch (type) {
            case PARAGRAPH -> {
                ParagraphBlock.Builder builder = ParagraphBlock.builder()
                    .name(name)
                    .severity(severity)
                    .occurrence(occurrence);
                if (lines != null) {
                    builder.lines(lines);
                }
                yield builder.build();
            }
            case LISTING -> ListingBlock.builder()
                .name(name)
                .severity(severity)
                .occurrence(occurrence)
                .build();
            case TABLE -> TableBlock.builder()
                .name(name)
                .severity(severity)
                .occurrence(occurrence)
                .build();
            case IMAGE -> ImageBlock.builder()
                .name(name)
                .severity(severity)
                .occurrence(occurrence)
                .build();
            case VERSE -> VerseBlock.builder()
                .name(name)
                .severity(severity)
                .occurrence(occurrence)
                .build();
        };
    }
    
    private BlockType parseBlockType(String value) {
        return switch (value.toLowerCase()) {
            case "paragraph" -> BlockType.PARAGRAPH;
            case "listing" -> BlockType.LISTING;
            case "table" -> BlockType.TABLE;
            case "image" -> BlockType.IMAGE;
            case "verse" -> BlockType.VERSE;
            default -> throw new IllegalArgumentException("Unknown block type: " + value);
        };
    }
    
    private Severity parseSeverity(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "error" -> Severity.ERROR;
            case "warn", "warning" -> Severity.WARN;
            case "info" -> Severity.INFO;
            default -> throw new IllegalArgumentException("Unknown severity: " + value);
        };
    }
    
    private static class CustomConstructor extends Constructor {
        public CustomConstructor(LoaderOptions options) {
            super(options);
        }
    }
}