package com.example.linter.validator.block;

import com.example.linter.Linter;
import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.loader.ConfigurationLoader;
import com.example.linter.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VideoBlock Integration Tests")
class VideoBlockIntegrationTest {

    private Linter linter;
    private ConfigurationLoader configLoader;

    @BeforeEach
    void setUp() {
        linter = new Linter();
        configLoader = new ConfigurationLoader();
    }

    @Test
    @DisplayName("should validate video blocks with full configuration")
    void shouldValidateVideoBlocksWithFullConfiguration() throws IOException {
        // Given
        try (InputStream configStream = getClass().getResourceAsStream("/video-block-config.yaml")) {
            LinterConfiguration config = configLoader.loadConfiguration(configStream);
            
            // When
            ValidationResult result = linter.validateFile(
                    Paths.get("src/test/resources/video-block-test.adoc"),
                    config
            );

            // Then
            assertNotNull(result);
            assertFalse(result.getMessages().isEmpty());

            // Check for expected validation errors
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Video autoplay is not allowed")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Video controls are required")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Video caption is required")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("exceeds maximum 50")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("less than minimum")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("does not match pattern")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Video poster image is required")));
            
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Video URL is required")));
        }
    }

    @Test
    @DisplayName("should detect video blocks correctly")
    void shouldDetectVideoBlocksCorrectly() throws IOException {
        // Given
        try (InputStream configStream = getClass().getResourceAsStream("/video-block-config.yaml")) {
            LinterConfiguration config = configLoader.loadConfiguration(configStream);
            
            // When
            ValidationResult result = linter.validateFile(
                    Paths.get("src/test/resources/video-block-test.adoc"),
                    config
            );

            // Then
            // Check that video blocks are being detected by looking for video-specific validation messages
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getRuleId() != null && m.getRuleId().startsWith("video-")));
        }
    }

    @Test
    @DisplayName("should apply severity hierarchy correctly")
    void shouldApplySeverityHierarchyCorrectly() throws IOException {
        // Given
        try (InputStream configStream = getClass().getResourceAsStream("/video-block-config.yaml")) {
            LinterConfiguration config = configLoader.loadConfiguration(configStream);
            
            // When
            ValidationResult result = linter.validateFile(
                    Paths.get("src/test/resources/video-block-test.adoc"),
                    config
            );

            // Then
            // URL validation should use ERROR severity from nested config
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getRuleId().equals("video-url-pattern") 
                            && m.getSeverity() == Severity.ERROR));
            
            // Width validation should use WARN severity from nested config
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getRuleId().equals("video-width-min") 
                            && m.getSeverity() == Severity.WARN));
            
            // Options validation should use ERROR severity from nested config
            assertTrue(result.getMessages().stream()
                    .anyMatch(m -> m.getRuleId().equals("video-autoplay-not-allowed") 
                            && m.getSeverity() == Severity.ERROR));
        }
    }

    @Test
    @DisplayName("should validate minimum and maximum occurrences")
    void shouldValidateMinimumAndMaximumOccurrences() throws IOException {
        // Given
        try (InputStream configStream = getClass().getResourceAsStream("/video-block-config.yaml")) {
            LinterConfiguration config = configLoader.loadConfiguration(configStream);
            
            // When
            ValidationResult result = linter.validateFile(
                    Paths.get("src/test/resources/video-block-test.adoc"),
                    config
            );

            // Then
            // The "Valid Video Blocks" section requires exactly 2 video blocks
            // The test file has 2 video blocks in this section, so there should be no occurrence errors
            assertFalse(result.getMessages().stream()
                    .anyMatch(m -> m.getMessage().contains("Valid Video Blocks") 
                            && m.getMessage().contains("occurrences")));
        }
    }

    @Test
    @DisplayName("should validate video blocks with minimal configuration")
    void shouldValidateVideoBlocksWithMinimalConfiguration() throws IOException {
        // Given
        String minimalConfig = """
                document:
                  sections:
                    - title: "Videos"
                      blocks:
                        - type: VIDEO
                """;
        
        LinterConfiguration config = configLoader.loadConfiguration(
                new java.io.ByteArrayInputStream(minimalConfig.getBytes())
        );
        
        // When
        ValidationResult result = linter.validateFile(
                Paths.get("src/test/resources/video-block-test.adoc"),
                config
        );
        
        // Then
        assertNotNull(result);
        // With minimal config, only structural validation should occur
        // No video-specific validation rules should trigger
        assertFalse(result.getMessages().stream()
                .anyMatch(m -> m.getRuleId() != null && m.getRuleId().startsWith("video-")));
    }
}