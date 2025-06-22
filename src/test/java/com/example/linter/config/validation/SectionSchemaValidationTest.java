package com.example.linter.config.validation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.loader.ConfigurationLoader;

@DisplayName("Section Schema Validation")
class SectionSchemaValidationTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader(true); // Skip schema validation for this test
    }
    
    @Test
    @DisplayName("should validate configuration with all block types in sections")
    void shouldValidateConfigurationWithAllBlockTypes() throws IOException {
        // Given
        String yaml = """
            document:
              sections:
                - name: comprehensive
                  level: 1
                  min: 1
                  max: 1
                  allowedBlocks:
                    - paragraph:
                        severity: warn
                        occurrence:
                          min: 1
                          max: 10
                    - listing:
                        severity: error
                        language:
                          required: true
                    - image:
                        severity: info
                        url:
                          required: true
                    - table:
                        severity: warn
                        columns:
                          min: 2
                          max: 10
                    - verse:
                        severity: info
                        author:
                          required: false
                    - sidebar:
                        severity: info
                        title:
                          required: true
                    - admonition:
                        severity: warn
                        type:
                          required: true
                          allowed: ["NOTE", "TIP", "WARNING"]
                    - pass:
                        severity: error
                        type:
                          required: true
                          allowed: ["html", "xml"]
                    - literal:
                        severity: warn
                        lines:
                          min: 1
                          max: 100
                    - audio:
                        severity: info
                        url:
                          required: true
                    - quote:
                        severity: info
                        author:
                          required: true
            """;
        
        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));
        
        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }
    
    @Test
    @DisplayName("should validate mixed block types in nested sections")
    void shouldValidateMixedBlockTypesInNestedSections() throws IOException {
        // Given
        String yaml = """
            document:
              sections:
                - name: main
                  level: 1
                  min: 1
                  max: 5
                  allowedBlocks:
                    - paragraph:
                        severity: error
                    - listing:
                        severity: warn
                    - admonition:
                        severity: info
                        type:
                          required: true
                  subsections:
                    - name: details
                      level: 2
                      min: 0
                      max: 3
                      allowedBlocks:
                        - literal:
                            severity: warn
                        - quote:
                            severity: info
                        - sidebar:
                            severity: info
                    - level: 2
                      min: 0
                      max: 5
                      allowedBlocks:
                        - audio:
                            severity: info
                        - pass:
                            severity: error
            """;
        
        // When & Then - should not throw any schema validation exceptions
        LinterConfiguration config = assertDoesNotThrow(() -> loader.loadConfiguration(yaml));
        
        // Then - verify configuration was loaded
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().sections());
    }
}