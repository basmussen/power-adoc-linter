package com.example.linter.config.output;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests for OutputConfigurationLoader.
 */
@DisplayName("OutputConfigurationLoader Tests")
class OutputConfigurationLoaderTest {
    
    private OutputConfigurationLoader loader;
    private OutputConfigurationLoader loaderWithoutValidation;
    
    @BeforeEach
    void setUp() {
        loader = new OutputConfigurationLoader();
        loaderWithoutValidation = new OutputConfigurationLoader(true);
    }
    
    @Nested
    @DisplayName("File Loading Tests")
    class FileLoadingTests {
        
        @TempDir
        Path tempDir;
        
        @Test
        @DisplayName("should load valid configuration from file")
        void shouldLoadValidConfigurationFromFile() throws IOException {
            // Given
            String yaml = """
                output:
                  format: enhanced
                  display:
                    useColors: true
                    contextLines: 3
                    showRuleId: true
                    showLineNumbers: true
                    highlightStyle: underline
                """;
            
            Path configFile = tempDir.resolve("output.yaml");
            Files.writeString(configFile, yaml);
            
            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(configFile.toString());
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertEquals(3, config.getDisplay().getContextLines());
            assertTrue(config.getDisplay().isShowRuleId());
            assertTrue(config.getDisplay().isShowLineNumbers());
            assertEquals(HighlightStyle.UNDERLINE, config.getDisplay().getHighlightStyle());
        }
        
        @Test
        @DisplayName("should throw exception for non-existent file")
        void shouldThrowExceptionForNonExistentFile() {
            // When/Then
            assertThrows(IOException.class, () -> 
                loader.loadConfiguration("/non/existent/file.yaml")
            );
        }
    }
    
    @Nested
    @DisplayName("Stream Loading Tests")
    class StreamLoadingTests {
        
        @Test
        @DisplayName("should load configuration with all features")
        void shouldLoadConfigurationWithAllFeatures() throws IOException {
            // Given
            String yaml = """
                output:
                  format: enhanced
                  display:
                    useColors: true
                    contextLines: 5
                    showRuleId: true
                    showLineNumbers: true
                    highlightStyle: box
                  errorGrouping:
                    enabled: true
                    groupBySeverity: true
                    groupByRule: true
                    groupByFile: false
                    maxGroupSize: 20
                    showSampleErrors: 5
                    collapseThreshold: 10
                  summary:
                    enabled: true
                    showStatistics: true
                    showMostCommon: true
                    showFileList: true
                    showAutoFixHint: true
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(input);
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            
            DisplayConfig display = config.getDisplay();
            assertTrue(display.isUseColors());
            assertEquals(5, display.getContextLines());
            assertTrue(display.isShowRuleId());
            assertTrue(display.isShowLineNumbers());
            assertEquals(HighlightStyle.BOX, display.getHighlightStyle());
            
            ErrorGroupingConfig grouping = config.getErrorGrouping();
            assertTrue(grouping.isEnabled());
            assertTrue(grouping.isGroupBySeverity());
            assertTrue(grouping.isGroupByRule());
            assertFalse(grouping.isGroupByFile());
            assertEquals(20, grouping.getMaxGroupSize());
            assertEquals(5, grouping.getShowSampleErrors());
            assertEquals(10, grouping.getCollapseThreshold());
            
            SummaryConfig summary = config.getSummary();
            assertTrue(summary.isEnabled());
            assertTrue(summary.isShowStatistics());
            assertTrue(summary.isShowMostCommon());
            assertTrue(summary.isShowFileList());
            assertTrue(summary.isShowAutoFixHint());
        }
        
        @Test
        @DisplayName("should load minimal configuration")
        void shouldLoadMinimalConfiguration() throws IOException {
            // Given
            String yaml = """
                output:
                  format: simple
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(input);
            
            // Then
            assertEquals(OutputFormat.SIMPLE, config.getFormat());
            assertNotNull(config.getDisplay());
            assertNotNull(config.getErrorGrouping());
            assertNotNull(config.getSummary());
        }
        
        @Test
        @DisplayName("should load compact configuration")
        void shouldLoadCompactConfiguration() throws IOException {
            // Given
            String yaml = """
                output:
                  format: compact
                  display:
                    useColors: false
                    contextLines: 0
                    highlightStyle: none
                  errorGrouping:
                    enabled: false
                  summary:
                    enabled: false
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When
            OutputConfiguration config = loaderWithoutValidation.loadConfiguration(input);
            
            // Then
            assertEquals(OutputFormat.COMPACT, config.getFormat());
            assertFalse(config.getDisplay().isUseColors());
            assertEquals(0, config.getDisplay().getContextLines());
            assertEquals(HighlightStyle.NONE, config.getDisplay().getHighlightStyle());
            assertFalse(config.getErrorGrouping().isEnabled());
            assertFalse(config.getSummary().isEnabled());
        }
    }
    
    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {
        
        @Test
        @DisplayName("should return default configuration")
        void shouldReturnDefaultConfiguration() {
            // When
            OutputConfiguration config = loader.getDefaultConfiguration();
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertTrue(config.getErrorGrouping().isEnabled());
            assertTrue(config.getSummary().isEnabled());
        }
        
        @Test
        @DisplayName("should return compact configuration")
        void shouldReturnCompactConfiguration() {
            // When
            OutputConfiguration config = loader.getCompactConfiguration();
            
            // Then
            assertEquals(OutputFormat.COMPACT, config.getFormat());
            assertFalse(config.getDisplay().isUseColors());
            assertFalse(config.getErrorGrouping().isEnabled());
            assertFalse(config.getSummary().isEnabled());
        }
    }
    
    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {
        
        @Test
        @DisplayName("should fail validation for invalid format")
        void shouldFailValidationForInvalidFormat() {
            // Given
            String yaml = """
                output:
                  format: invalid
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When/Then
            assertThrows(OutputConfigurationException.class, () ->
                loader.loadConfiguration(input)
            );
        }
        
        @Test
        @DisplayName("should fail validation for invalid context lines")
        void shouldFailValidationForInvalidContextLines() {
            // Given
            String yaml = """
                output:
                  format: enhanced
                  display:
                    contextLines: 20
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When/Then
            assertThrows(OutputConfigurationException.class, () ->
                loader.loadConfiguration(input)
            );
        }
        
        @Test
        @DisplayName("should pass validation when skipped")
        void shouldPassValidationWhenSkipped() throws IOException {
            // Given - invalid configuration that would fail validation
            String yaml = """
                output:
                  format: invalid
                  display:
                    contextLines: 99
                """;
            
            InputStream input = new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
            
            // When - using loader with validation skipped
            // Then - should not throw
            assertDoesNotThrow(() -> 
                loaderWithoutValidation.loadConfiguration(input)
            );
        }
    }
}