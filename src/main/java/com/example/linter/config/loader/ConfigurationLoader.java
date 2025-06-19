package com.example.linter.config.loader;

import com.example.linter.config.*;
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
        return new LinterConfiguration(document);
    }
    
    private DocumentConfiguration parseDocumentConfiguration(Map<String, Object> raw) {
        Map<String, Object> metadataRaw = (Map<String, Object>) raw.get("metadata");
        MetadataConfiguration metadata = parseMetadataConfiguration(metadataRaw);
        
        List<Map<String, Object>> sectionsRaw = (List<Map<String, Object>>) raw.get("sections");
        List<SectionRule> sections = parseSectionRules(sectionsRaw);
        
        return new DocumentConfiguration(metadata, sections);
    }
    
    private MetadataConfiguration parseMetadataConfiguration(Map<String, Object> raw) {
        List<Map<String, Object>> attributesRaw = (List<Map<String, Object>>) raw.get("attributes");
        List<AttributeRule> attributes = new ArrayList<>();
        
        for (Map<String, Object> attrRaw : attributesRaw) {
            attributes.add(parseAttributeRule(attrRaw));
        }
        
        return new MetadataConfiguration(attributes);
    }
    
    private AttributeRule parseAttributeRule(Map<String, Object> raw) {
        return new AttributeRule(
            (String) raw.get("name"),
            (Integer) raw.get("order"),
            Boolean.TRUE.equals(raw.get("required")),
            (Integer) raw.get("minLength"),
            (Integer) raw.get("maxLength"),
            (String) raw.get("pattern"),
            parseSeverity((String) raw.get("severity"))
        );
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
            title = new TitleRule((String) titleRaw.get("pattern"));
        }
        
        List<AllowedBlock> allowedBlocks = new ArrayList<>();
        if (raw.containsKey("allowedBlocks")) {
            List<Map<String, Object>> blocksRaw = (List<Map<String, Object>>) raw.get("allowedBlocks");
            for (Map<String, Object> blockRaw : blocksRaw) {
                allowedBlocks.add(parseAllowedBlock(blockRaw));
            }
        }
        
        List<SectionRule> subsections = new ArrayList<>();
        if (raw.containsKey("subsections")) {
            subsections = parseSectionRules((List<Map<String, Object>>) raw.get("subsections"));
        }
        
        return new SectionRule(
            (String) raw.get("name"),
            (Integer) raw.get("order"),
            (Integer) raw.get("level"),
            raw.get("min") != null ? (Integer) raw.get("min") : 0,
            raw.get("max") != null ? (Integer) raw.get("max") : Integer.MAX_VALUE,
            title,
            allowedBlocks,
            subsections
        );
    }
    
    private AllowedBlock parseAllowedBlock(Map<String, Object> raw) {
        // The YAML structure has block type as key (e.g., "paragraph:", "listing:")
        Map.Entry<String, Object> entry = raw.entrySet().iterator().next();
        String blockTypeStr = entry.getKey();
        Map<String, Object> blockData = (Map<String, Object>) entry.getValue();
        
        BlockType type = parseBlockType(blockTypeStr);
        String name = (String) blockData.get("name");
        Severity severity = parseSeverity((String) blockData.get("severity"));
        
        OccurrenceRule occurrence = null;
        if (blockData.containsKey("occurrence")) {
            Map<String, Object> occRaw = (Map<String, Object>) blockData.get("occurrence");
            occurrence = new OccurrenceRule(
                (Integer) occRaw.get("order"),
                occRaw.get("min") != null ? (Integer) occRaw.get("min") : 0,
                occRaw.get("max") != null ? (Integer) occRaw.get("max") : Integer.MAX_VALUE,
                occRaw.containsKey("severity") ? parseSeverity((String) occRaw.get("severity")) : null
            );
        }
        
        LineRule lines = null;
        if (blockData.containsKey("lines")) {
            Map<String, Object> linesRaw = (Map<String, Object>) blockData.get("lines");
            lines = new LineRule(
                (Integer) linesRaw.get("min"),
                (Integer) linesRaw.get("max"),
                linesRaw.containsKey("severity") ? parseSeverity((String) linesRaw.get("severity")) : null
            );
        }
        
        // Handle direct min/max when no occurrence block
        if (occurrence == null && (blockData.containsKey("min") || blockData.containsKey("max"))) {
            occurrence = new OccurrenceRule(
                null,
                blockData.get("min") != null ? (Integer) blockData.get("min") : 0,
                blockData.get("max") != null ? (Integer) blockData.get("max") : Integer.MAX_VALUE,
                null
            );
        }
        
        return new AllowedBlock(type, name, severity, occurrence, lines);
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