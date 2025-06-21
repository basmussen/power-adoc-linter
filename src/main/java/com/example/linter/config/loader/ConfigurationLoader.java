package com.example.linter.config.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.validation.RuleSchemaValidator;
import com.example.linter.config.validation.RuleValidationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Configuration loader using Jackson for YAML parsing.
 * Provides type-safe deserialization directly to domain objects.
 */
public class ConfigurationLoader {
    
    private static final Logger logger = LogManager.getLogger(ConfigurationLoader.class);
    
    private final ObjectMapper yamlMapper;
    private final RuleSchemaValidator schemaValidator;
    private final boolean skipRuleSchemaValidation;
    
    public ConfigurationLoader() {
        this(false);
    }
    
    public ConfigurationLoader(boolean skipRuleSchemaValidation) {
        this.yamlMapper = createYamlMapper();
        this.skipRuleSchemaValidation = skipRuleSchemaValidation;
        
        if (!skipRuleSchemaValidation) {
            this.schemaValidator = new RuleSchemaValidator();
        } else {
            this.schemaValidator = null;
            logger.warn("Rule configuration schema validation is DISABLED");
        }
    }
    
    /**
     * Creates and configures the Jackson ObjectMapper for YAML parsing.
     */
    private ObjectMapper createYamlMapper() {
        YAMLFactory yamlFactory = new YAMLFactory();
        ObjectMapper mapper = new ObjectMapper(yamlFactory);
        
        // Configure mapper
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // Be lenient for backward compatibility
        mapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);
        mapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        
        // Register custom module for block list deserialization
        SimpleModule module = new SimpleModule();
        // BlockListDeserializer is already registered via @JsonDeserialize annotation
        mapper.registerModule(module);
        
        return mapper;
    }
    
    /**
     * Loads configuration from a file path.
     * Validates against schema if validation is enabled.
     */
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
    
    /**
     * Loads configuration from a string.
     */
    public LinterConfiguration loadConfiguration(String yamlContent) {
        try {
            if (yamlContent == null || yamlContent.trim().isEmpty()) {
                throw new ConfigurationException("Configuration is empty");
            }
            
            LinterConfiguration config = yamlMapper.readValue(yamlContent, LinterConfiguration.class);
            
            // Check for missing document section
            if (config.document() == null) {
                // Check if there are other keys that suggest this is a malformed config
                try {
                    JsonNode tree = yamlMapper.readTree(yamlContent);
                    if (tree.isObject() && tree.size() > 0 && !tree.has("document")) {
                        throw new ConfigurationException("Missing required 'document' section in configuration");
                    }
                } catch (IOException ex) {
                    // Ignore and continue
                }
            }
            
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to parse YAML configuration: " + e.getMessage(), e);
        }
    }
    
    /**
     * Loads configuration from an input stream.
     */
    public LinterConfiguration loadConfiguration(InputStream inputStream) {
        try {
            LinterConfiguration config = yamlMapper.readValue(inputStream, LinterConfiguration.class);
            if (config == null) {
                throw new ConfigurationException("Configuration file is empty");
            }
            return config;
        } catch (IOException e) {
            throw new ConfigurationException("Failed to load configuration: " + e.getMessage(), e);
        }
    }
}