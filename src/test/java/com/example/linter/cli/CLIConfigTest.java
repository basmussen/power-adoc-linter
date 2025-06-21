package com.example.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;

@DisplayName("CLIConfig")
class CLIConfigTest {
    
    @Test
    @DisplayName("should build with default values")
    void shouldBuildWithDefaultValues() {
        // Given/When
        CLIConfig config = CLIConfig.builder()
            .input(Paths.get("test.adoc"))
            .build();
        
        // Then
        assertEquals(Paths.get("test.adoc"), config.getInput());
        assertNull(config.getConfigFile());
        assertEquals("console", config.getReportFormat());
        assertNull(config.getReportOutput());
        assertTrue(config.isRecursive());
        assertEquals("*.adoc", config.getPattern());
        assertEquals(Severity.ERROR, config.getFailLevel());
        assertFalse(config.isOutputToFile());
    }
    
    @Test
    @DisplayName("should build with all values set")
    void shouldBuildWithAllValuesSet() {
        // Given
        Path input = Paths.get("/docs");
        Path configFile = Paths.get("config.yaml");
        Path output = Paths.get("report.json");
        
        // When
        CLIConfig config = CLIConfig.builder()
            .input(input)
            .configFile(configFile)
            .reportFormat("json")
            .reportOutput(output)
            .recursive(false)
            .pattern("**/*.asciidoc")
            .failLevel(Severity.WARN)
            .build();
        
        // Then
        assertEquals(input, config.getInput());
        assertEquals(configFile, config.getConfigFile());
        assertEquals("json", config.getReportFormat());
        assertEquals(output, config.getReportOutput());
        assertFalse(config.isRecursive());
        assertEquals("**/*.asciidoc", config.getPattern());
        assertEquals(Severity.WARN, config.getFailLevel());
        assertTrue(config.isOutputToFile());
    }
    
    @Test
    @DisplayName("should require input")
    void shouldRequireInput() {
        // When/Then
        assertThrows(NullPointerException.class, () ->
            CLIConfig.builder().build());
    }
    
    @Test
    @DisplayName("should require report format")
    void shouldRequireReportFormat() {
        // When/Then
        assertThrows(NullPointerException.class, () ->
            CLIConfig.builder()
                .input(Paths.get("test.adoc"))
                .reportFormat(null)
                .build());
    }
    
    @Test
    @DisplayName("should require pattern")
    void shouldRequirePattern() {
        // When/Then
        assertThrows(NullPointerException.class, () ->
            CLIConfig.builder()
                .input(Paths.get("test.adoc"))
                .pattern(null)
                .build());
    }
    
    @Test
    @DisplayName("should require fail level")
    void shouldRequireFailLevel() {
        // When/Then
        assertThrows(NullPointerException.class, () ->
            CLIConfig.builder()
                .input(Paths.get("test.adoc"))
                .failLevel(null)
                .build());
    }
}