package com.example.linter.config.blocks;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("VideoBlock Simple Tests")
class VideoBlockSimpleTest {

    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    @Test
    @DisplayName("should create minimal video block")
    void shouldCreateMinimalVideoBlock() {
        // When
        VideoBlock videoBlock = VideoBlock.builder()
                .severity(Severity.WARN)
                .build();

        // Then
        assertNull(videoBlock.getName());
        assertEquals(Severity.WARN, videoBlock.getSeverity());
        assertNull(videoBlock.getOccurrence());
        assertNull(videoBlock.getUrl());
        assertNull(videoBlock.getWidth());
        assertNull(videoBlock.getHeight());
        assertNull(videoBlock.getPoster());
        assertNull(videoBlock.getOptions());
        assertNull(videoBlock.getCaption());
        assertEquals(BlockType.VIDEO, videoBlock.getType());
    }

    @Test
    @DisplayName("should create video block with URL validation")
    void shouldCreateVideoBlockWithUrlValidation() {
        // Given
        VideoBlock.UrlConfig urlConfig = VideoBlock.UrlConfig.builder()
                .required(true)
                .pattern("^https://.*\\.mp4$")
                .severity(Severity.ERROR)
                .build();

        // When
        VideoBlock videoBlock = VideoBlock.builder()
                .name("Test Video")
                .severity(Severity.WARN)
                .url(urlConfig)
                .build();

        // Then
        assertEquals("Test Video", videoBlock.getName());
        assertEquals(Severity.WARN, videoBlock.getSeverity());
        assertNotNull(videoBlock.getUrl());
        assertTrue(videoBlock.getUrl().isRequired());
        assertEquals("^https://.*\\.mp4$", videoBlock.getUrl().getPattern().pattern());
        assertEquals(Severity.ERROR, videoBlock.getUrl().getSeverity());
    }

    @Test
    @DisplayName("should create video block with dimension validation")
    void shouldCreateVideoBlockWithDimensionValidation() {
        // Given
        VideoBlock.DimensionConfig widthConfig = VideoBlock.DimensionConfig.builder()
                .minValue(640)
                .maxValue(1920)
                .severity(Severity.WARN)
                .build();

        VideoBlock.DimensionConfig heightConfig = VideoBlock.DimensionConfig.builder()
                .minValue(480)
                .maxValue(1080)
                .build();

        // When
        VideoBlock videoBlock = VideoBlock.builder()
                .severity(Severity.ERROR)
                .width(widthConfig)
                .height(heightConfig)
                .build();

        // Then
        assertNotNull(videoBlock.getWidth());
        assertEquals(640, videoBlock.getWidth().getMinValue());
        assertEquals(1920, videoBlock.getWidth().getMaxValue());
        assertEquals(Severity.WARN, videoBlock.getWidth().getSeverity());
        
        assertNotNull(videoBlock.getHeight());
        assertEquals(480, videoBlock.getHeight().getMinValue());
        assertEquals(1080, videoBlock.getHeight().getMaxValue());
        assertNull(videoBlock.getHeight().getSeverity());
    }

    @Test
    @DisplayName("should deserialize video block from YAML with nested options")
    void shouldDeserializeVideoBlockFromYamlWithNestedOptions() throws IOException {
        // Given
        String yaml = """
                name: "Test Video"
                severity: ERROR
                occurrence:
                  min: 1
                  max: 3
                url:
                  required: true
                  pattern: "^https://.*"
                width:
                  minValue: 640
                  maxValue: 1920
                height:
                  minValue: 480
                  maxValue: 1080
                options:
                  autoplay:
                    allowed: false
                    severity: ERROR
                  controls:
                    required: true
                    severity: WARN
                caption:
                  required: true
                  minLength: 10
                  maxLength: 100
                """;

        // When
        VideoBlock videoBlock = mapper.readValue(yaml, VideoBlock.class);

        // Then
        assertEquals("Test Video", videoBlock.getName());
        assertEquals(Severity.ERROR, videoBlock.getSeverity());
        
        assertNotNull(videoBlock.getOccurrence());
        assertEquals(1, videoBlock.getOccurrence().min());
        assertEquals(3, videoBlock.getOccurrence().max());
        
        assertNotNull(videoBlock.getUrl());
        assertTrue(videoBlock.getUrl().isRequired());
        
        assertNotNull(videoBlock.getWidth());
        assertEquals(640, videoBlock.getWidth().getMinValue());
        assertEquals(1920, videoBlock.getWidth().getMaxValue());
        
        assertNotNull(videoBlock.getOptions());
        assertNotNull(videoBlock.getOptions().getAutoplay());
        assertEquals(false, videoBlock.getOptions().getAutoplay().getAllowed());
        assertEquals(Severity.ERROR, videoBlock.getOptions().getAutoplay().getSeverity());
        assertNotNull(videoBlock.getOptions().getControls());
        assertTrue(videoBlock.getOptions().getControls().isRequired());
        assertEquals(Severity.WARN, videoBlock.getOptions().getControls().getSeverity());
        
        assertNotNull(videoBlock.getCaption());
        assertTrue(videoBlock.getCaption().isRequired());
        assertEquals(10, videoBlock.getCaption().getMinLength());
        assertEquals(100, videoBlock.getCaption().getMaxLength());
    }

    @Test
    @DisplayName("should serialize video block to YAML")
    void shouldSerializeVideoBlockToYaml() throws IOException {
        // Given
        VideoBlock videoBlock = VideoBlock.builder()
                .name("Video Block")
                .severity(Severity.ERROR)
                .url(VideoBlock.UrlConfig.builder()
                        .required(true)
                        .pattern("^https://.*\\.mp4$")
                        .build())
                .build();

        // When
        String yaml = mapper.writeValueAsString(videoBlock);

        // Then
        assertTrue(yaml.contains("name: \"Video Block\""));
        assertTrue(yaml.contains("error"));
        assertTrue(yaml.contains("url:"));
        assertTrue(yaml.contains("required: true"));
        assertTrue(yaml.contains("pattern: \"^https://.*\\\\.mp4$\""));
    }
}