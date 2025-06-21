package com.example.linter.config.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import com.example.linter.config.BlockType;
import com.example.linter.config.DocumentConfiguration;
import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.MetadataConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.ImageBlock;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.blocks.TableBlock;
import com.example.linter.config.blocks.VerseBlock;
import com.example.linter.config.rule.AttributeConfig;
import com.example.linter.config.rule.OccurrenceConfig;
import com.example.linter.config.rule.SectionConfig;
import com.example.linter.config.rule.TitleConfig;
import com.example.linter.config.validation.RuleSchemaValidator;
import com.example.linter.config.validation.RuleValidationException;

public class ConfigurationLoader {
    
    private final Yaml yaml;
    private final RuleSchemaValidator schemaValidator;
    private final boolean skipRuleSchemaValidation;
    
    public ConfigurationLoader() {
        this(false);
    }
    
    public ConfigurationLoader(boolean skipRuleSchemaValidation) {
        LoaderOptions loaderOptions = new LoaderOptions();
        CustomConstructor constructor = new CustomConstructor(loaderOptions);
        DumperOptions dumperOptions = new DumperOptions();
        Representer representer = new Representer(dumperOptions);
        
        this.yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
        this.skipRuleSchemaValidation = skipRuleSchemaValidation;
        
        if (!skipRuleSchemaValidation) {
            this.schemaValidator = new RuleSchemaValidator();
        } else {
            this.schemaValidator = null;
            System.err.println("WARNING: Rule configuration schema validation is DISABLED");
        }
    }
    
    public LinterConfiguration loadConfiguration(Path configPath) throws IOException {
        // First: Validate user config against schema
        if (!skipRuleSchemaValidation && schemaValidator != null) {
            try {
                schemaValidator.validateUserConfig(configPath);
            } catch (RuleValidationException e) {
                throw new ConfigurationException(
                    "User configuration does not match schema: " + e.getMessage(), e);
            }
        }
        
        // Then: Parse the validated config
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            return loadConfiguration(inputStream);
        }
    }
    
    public LinterConfiguration loadConfiguration(String yamlContent) {
        try {
            Map<String, Object> rawConfig = yaml.load(yamlContent);
            if (rawConfig == null || rawConfig.isEmpty()) {
                throw new ConfigurationException("Configuration is empty");
            }
            return parseConfiguration(rawConfig);
        } catch (Exception e) {
            if (e instanceof ConfigurationException) {
                throw e;
            }
            throw new ConfigurationException("Failed to parse YAML configuration: " + e.getMessage(), e);
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
        List<SectionConfig> sections = parseSectionRules(sectionsRaw);
        
        return DocumentConfiguration.builder()
            .metadata(metadata)
            .sections(sections)
            .build();
    }
    
    private MetadataConfiguration parseMetadataConfiguration(Map<String, Object> raw) {
        List<Map<String, Object>> attributesRaw = (List<Map<String, Object>>) raw.get("attributes");
        List<AttributeConfig> attributes = new ArrayList<>();
        
        for (Map<String, Object> attrRaw : attributesRaw) {
            attributes.add(parseAttributeRule(attrRaw));
        }
        
        return MetadataConfiguration.builder()
            .attributes(attributes)
            .build();
    }
    
    private AttributeConfig parseAttributeRule(Map<String, Object> raw) {
        return AttributeConfig.builder()
            .name((String) raw.get("name"))
            .order((Integer) raw.get("order"))
            .required(Boolean.TRUE.equals(raw.get("required")))
            .minLength((Integer) raw.get("minLength"))
            .maxLength((Integer) raw.get("maxLength"))
            .pattern((String) raw.get("pattern"))
            .severity(parseSeverity((String) raw.get("severity")))
            .build();
    }
    
    private List<SectionConfig> parseSectionRules(List<Map<String, Object>> sectionsRaw) {
        List<SectionConfig> sections = new ArrayList<>();
        if (sectionsRaw != null) {
            for (Map<String, Object> sectionRaw : sectionsRaw) {
                sections.add(parseSectionRule(sectionRaw));
            }
        }
        return sections;
    }
    
    private SectionConfig parseSectionRule(Map<String, Object> raw) {
        TitleConfig title = null;
        if (raw.containsKey("title")) {
            Map<String, Object> titleRaw = (Map<String, Object>) raw.get("title");
            title = TitleConfig.builder()
                .pattern((String) titleRaw.get("pattern"))
                .build();
        }
        
        List<Block> allowedBlocks = new ArrayList<>();
        if (raw.containsKey("allowedBlocks")) {
            List<Map<String, Object>> blocksRaw = (List<Map<String, Object>>) raw.get("allowedBlocks");
            for (Map<String, Object> blockRaw : blocksRaw) {
                allowedBlocks.add(parseBlock(blockRaw));
            }
        }
        
        List<SectionConfig> subsections = new ArrayList<>();
        if (raw.containsKey("subsections")) {
            subsections = parseSectionRules((List<Map<String, Object>>) raw.get("subsections"));
        }
        
        SectionConfig.Builder builder = SectionConfig.builder()
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
    
    private Block parseBlock(Map<String, Object> raw) {
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
        
        OccurrenceConfig occurrence = null;
        if (blockData.containsKey("occurrence")) {
            Map<String, Object> occRaw = (Map<String, Object>) blockData.get("occurrence");
            OccurrenceConfig.Builder occBuilder = OccurrenceConfig.builder()
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
        
        com.example.linter.config.rule.LineConfig lines = null;
        if (blockData.containsKey("lines")) {
            Map<String, Object> linesRaw = (Map<String, Object>) blockData.get("lines");
            com.example.linter.config.rule.LineConfig.Builder lineBuilder = com.example.linter.config.rule.LineConfig.builder()
                .min((Integer) linesRaw.get("min"))
                .max((Integer) linesRaw.get("max"));
            
            if (linesRaw.containsKey("severity")) {
                lineBuilder.severity(parseSeverity((String) linesRaw.get("severity")));
            }
            
            lines = lineBuilder.build();
        }
        
        // Handle direct min/max when no occurrence block
        if (occurrence == null && (blockData.containsKey("min") || blockData.containsKey("max"))) {
            OccurrenceConfig.Builder occBuilder = OccurrenceConfig.builder();
            
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
            case LISTING -> {
                ListingBlock.Builder builder = ListingBlock.builder()
                    .name(name)
                    .severity(severity)
                    .occurrence(occurrence);
                
                // Parse language attribute
                if (blockData.containsKey("language")) {
                    Map<String, Object> langRaw = (Map<String, Object>) blockData.get("language");
                    ListingBlock.LanguageConfig.LanguageConfigBuilder langBuilder = ListingBlock.LanguageConfig.builder()
                        .required(Boolean.TRUE.equals(langRaw.get("required")))
                        .allowed((List<String>) langRaw.get("allowed"))
                        .severity(parseSeverity((String) langRaw.get("severity")));
                    builder.language(langBuilder.build());
                }
                
                // Parse title attribute
                if (blockData.containsKey("title")) {
                    Map<String, Object> titleRaw = (Map<String, Object>) blockData.get("title");
                    ListingBlock.TitleConfig.TitleConfigBuilder titleBuilder = ListingBlock.TitleConfig.builder()
                        .required(Boolean.TRUE.equals(titleRaw.get("required")))
                        .pattern((String) titleRaw.get("pattern"))
                        .severity(parseSeverity((String) titleRaw.get("severity")));
                    builder.title(titleBuilder.build());
                }
                
                // Parse callouts attribute
                if (blockData.containsKey("callouts")) {
                    Map<String, Object> calloutsRaw = (Map<String, Object>) blockData.get("callouts");
                    ListingBlock.CalloutsConfig.CalloutsConfigBuilder calloutsBuilder = ListingBlock.CalloutsConfig.builder()
                        .allowed(Boolean.TRUE.equals(calloutsRaw.get("allowed")))
                        .max((Integer) calloutsRaw.get("max"))
                        .severity(parseSeverity((String) calloutsRaw.get("severity")));
                    builder.callouts(calloutsBuilder.build());
                }
                
                // Lines were already parsed above
                if (lines != null) {
                    builder.lines(lines);
                }
                
                yield builder.build();
            }
            case TABLE -> {
                TableBlock.Builder builder = TableBlock.builder()
                    .name(name)
                    .severity(severity)
                    .occurrence(occurrence);
                
                // Parse columns attribute
                if (blockData.containsKey("columns")) {
                    Map<String, Object> colsRaw = (Map<String, Object>) blockData.get("columns");
                    TableBlock.DimensionConfig.DimensionConfigBuilder colsBuilder = TableBlock.DimensionConfig.builder()
                        .min((Integer) colsRaw.get("min"))
                        .max((Integer) colsRaw.get("max"))
                        .severity(parseSeverity((String) colsRaw.get("severity")));
                    builder.columns(colsBuilder.build());
                }
                
                // Parse rows attribute
                if (blockData.containsKey("rows")) {
                    Map<String, Object> rowsRaw = (Map<String, Object>) blockData.get("rows");
                    TableBlock.DimensionConfig.DimensionConfigBuilder rowsBuilder = TableBlock.DimensionConfig.builder()
                        .min((Integer) rowsRaw.get("min"))
                        .max((Integer) rowsRaw.get("max"))
                        .severity(parseSeverity((String) rowsRaw.get("severity")));
                    builder.rows(rowsBuilder.build());
                }
                
                // Parse header attribute
                if (blockData.containsKey("header")) {
                    Map<String, Object> headerRaw = (Map<String, Object>) blockData.get("header");
                    TableBlock.HeaderConfig.HeaderConfigBuilder headerBuilder = TableBlock.HeaderConfig.builder()
                        .required(Boolean.TRUE.equals(headerRaw.get("required")))
                        .pattern((String) headerRaw.get("pattern"))
                        .severity(parseSeverity((String) headerRaw.get("severity")));
                    builder.header(headerBuilder.build());
                }
                
                // Parse caption attribute
                if (blockData.containsKey("caption")) {
                    Map<String, Object> captionRaw = (Map<String, Object>) blockData.get("caption");
                    TableBlock.CaptionConfig.CaptionConfigBuilder captionBuilder = TableBlock.CaptionConfig.builder()
                        .required(Boolean.TRUE.equals(captionRaw.get("required")))
                        .pattern((String) captionRaw.get("pattern"))
                        .minLength((Integer) captionRaw.get("minLength"))
                        .maxLength((Integer) captionRaw.get("maxLength"))
                        .severity(parseSeverity((String) captionRaw.get("severity")));
                    builder.caption(captionBuilder.build());
                }
                
                // Parse format attribute
                if (blockData.containsKey("format")) {
                    Map<String, Object> formatRaw = (Map<String, Object>) blockData.get("format");
                    TableBlock.FormatConfig.FormatConfigBuilder formatBuilder = TableBlock.FormatConfig.builder()
                        .style((String) formatRaw.get("style"))
                        .borders((Boolean) formatRaw.get("borders"))
                        .severity(parseSeverity((String) formatRaw.get("severity")));
                    builder.format(formatBuilder.build());
                }
                
                yield builder.build();
            }
            case IMAGE -> {
                ImageBlock.Builder builder = ImageBlock.builder()
                    .name(name)
                    .severity(severity)
                    .occurrence(occurrence);
                
                // Parse url attribute
                if (blockData.containsKey("url")) {
                    Map<String, Object> urlRaw = (Map<String, Object>) blockData.get("url");
                    ImageBlock.UrlConfig.UrlConfigBuilder urlBuilder = ImageBlock.UrlConfig.builder()
                        .required(Boolean.TRUE.equals(urlRaw.get("required")))
                        .pattern((String) urlRaw.get("pattern"));
                    builder.url(urlBuilder.build());
                }
                
                // Parse height attribute
                if (blockData.containsKey("height")) {
                    Map<String, Object> heightRaw = (Map<String, Object>) blockData.get("height");
                    ImageBlock.DimensionConfig.DimensionConfigBuilder heightBuilder = ImageBlock.DimensionConfig.builder()
                        .required(Boolean.TRUE.equals(heightRaw.get("required")))
                        .minValue((Integer) heightRaw.get("minValue"))
                        .maxValue((Integer) heightRaw.get("maxValue"));
                    builder.height(heightBuilder.build());
                }
                
                // Parse width attribute
                if (blockData.containsKey("width")) {
                    Map<String, Object> widthRaw = (Map<String, Object>) blockData.get("width");
                    ImageBlock.DimensionConfig.DimensionConfigBuilder widthBuilder = ImageBlock.DimensionConfig.builder()
                        .required(Boolean.TRUE.equals(widthRaw.get("required")))
                        .minValue((Integer) widthRaw.get("minValue"))
                        .maxValue((Integer) widthRaw.get("maxValue"));
                    builder.width(widthBuilder.build());
                }
                
                // Parse alt attribute
                if (blockData.containsKey("alt")) {
                    Map<String, Object> altRaw = (Map<String, Object>) blockData.get("alt");
                    ImageBlock.AltTextConfig.AltTextConfigBuilder altBuilder = ImageBlock.AltTextConfig.builder()
                        .required(Boolean.TRUE.equals(altRaw.get("required")))
                        .minLength((Integer) altRaw.get("minLength"))
                        .maxLength((Integer) altRaw.get("maxLength"));
                    builder.alt(altBuilder.build());
                }
                
                yield builder.build();
            }
            case VERSE -> {
                VerseBlock.Builder builder = VerseBlock.builder()
                    .name(name)
                    .severity(severity)
                    .occurrence(occurrence);
                
                // Parse author attribute
                if (blockData.containsKey("author")) {
                    Map<String, Object> authorRaw = (Map<String, Object>) blockData.get("author");
                    VerseBlock.AuthorConfig.AuthorConfigBuilder authorBuilder = VerseBlock.AuthorConfig.builder()
                        .defaultValue((String) authorRaw.get("defaultValue"))
                        .minLength((Integer) authorRaw.get("minLength"))
                        .maxLength((Integer) authorRaw.get("maxLength"))
                        .pattern((String) authorRaw.get("pattern"))
                        .required(Boolean.TRUE.equals(authorRaw.get("required")));
                    builder.author(authorBuilder.build());
                }
                
                // Parse attribution attribute
                if (blockData.containsKey("attribution")) {
                    Map<String, Object> attrRaw = (Map<String, Object>) blockData.get("attribution");
                    VerseBlock.AttributionConfig.AttributionConfigBuilder attrBuilder = VerseBlock.AttributionConfig.builder()
                        .defaultValue((String) attrRaw.get("defaultValue"))
                        .minLength((Integer) attrRaw.get("minLength"))
                        .maxLength((Integer) attrRaw.get("maxLength"))
                        .pattern((String) attrRaw.get("pattern"))
                        .required(Boolean.TRUE.equals(attrRaw.get("required")));
                    builder.attribution(attrBuilder.build());
                }
                
                // Parse content attribute
                if (blockData.containsKey("content")) {
                    Map<String, Object> contentRaw = (Map<String, Object>) blockData.get("content");
                    VerseBlock.ContentConfig.ContentConfigBuilder contentBuilder = VerseBlock.ContentConfig.builder()
                        .minLength((Integer) contentRaw.get("minLength"))
                        .maxLength((Integer) contentRaw.get("maxLength"))
                        .pattern((String) contentRaw.get("pattern"))
                        .required(Boolean.TRUE.equals(contentRaw.get("required")));
                    builder.content(contentBuilder.build());
                }
                
                yield builder.build();
            }
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