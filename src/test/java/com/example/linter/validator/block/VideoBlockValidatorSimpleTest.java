package com.example.linter.validator.block;

import com.example.linter.config.Severity;
import com.example.linter.config.blocks.VideoBlock;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@DisplayName("VideoBlockValidator Simple Tests")
class VideoBlockValidatorSimpleTest {

    private VideoBlockValidator validator;
    private Block mockBlock;
    private StructuralNode mockParent;
    private BlockValidationContext context;

    @BeforeEach
    void setUp() {
        validator = new VideoBlockValidator();
        mockBlock = mock(Block.class);
        mockParent = mock(StructuralNode.class);
        context = mock(BlockValidationContext.class);
        when(mockBlock.getParent()).thenReturn(mockParent);
        when(mockBlock.getContext()).thenReturn("video");
        when(mockBlock.getSourceLocation()).thenReturn(null);
        
        // Mock context to return proper source location
        when(context.createLocation(any())).thenReturn(
            com.example.linter.validator.SourceLocation.builder()
                .filename("test.adoc")
                .startLine(1)
                .build()
        );
    }

    @Test
    @DisplayName("should validate required URL")
    void shouldValidateRequiredUrl() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.ERROR)
                .url(VideoBlock.UrlConfig.builder()
                        .required(true)
                        .severity(Severity.ERROR)
                        .build())
                .build();

        when(mockBlock.getAttribute("target")).thenReturn(null);
        when(mockBlock.getSource()).thenReturn(null);
        when(mockBlock.getContent()).thenReturn(null);

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(1, messages.size());
        assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        assertTrue(messages.get(0).getMessage().contains("Video URL is required"));
    }

    @Test
    @DisplayName("should validate URL pattern")
    void shouldValidateUrlPattern() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.WARN)
                .url(VideoBlock.UrlConfig.builder()
                        .pattern("^https://.*\\.(mp4|webm)$")
                        .severity(Severity.WARN)
                        .build())
                .build();

        when(mockBlock.getAttribute("target")).thenReturn("http://example.com/video.avi");

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(1, messages.size());
        assertEquals(Severity.WARN, messages.get(0).getSeverity());
        assertTrue(messages.get(0).getMessage().contains("does not match pattern"));
    }

    @Test
    @DisplayName("should validate width dimension")
    void shouldValidateWidthDimension() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.ERROR)
                .width(VideoBlock.DimensionConfig.builder()
                        .minValue(320)
                        .maxValue(1920)
                        .build())
                .build();

        when(mockBlock.getAttribute("width")).thenReturn("200");

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).getMessage().contains("Video width 200 is less than minimum"));
    }

    @Test
    @DisplayName("should validate poster required")
    void shouldValidatePosterRequired() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.ERROR)
                .poster(VideoBlock.PosterConfig.builder()
                        .required(true)
                        .severity(Severity.ERROR)
                        .build())
                .build();

        when(mockBlock.getAttribute("poster")).thenReturn(null);

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(1, messages.size());
        assertEquals(Severity.ERROR, messages.get(0).getSeverity());
        assertTrue(messages.get(0).getMessage().contains("Video poster image is required"));
    }

    @Test
    @DisplayName("should validate options with nested autoplay and controls")
    void shouldValidateOptionsWithNestedAutoplayAndControls() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.WARN)
                .options(VideoBlock.OptionsConfig.builder()
                        .autoplay(VideoBlock.AutoplayConfig.builder()
                                .allowed(false)
                                .severity(Severity.WARN)
                                .build())
                        .controls(VideoBlock.ControlsConfig.builder()
                                .required(true)
                                .severity(Severity.ERROR)
                                .build())
                        .build())
                .build();

        // Video blocks have individual option attributes
        when(mockBlock.getAttribute("autoplay-option")).thenReturn("");
        when(mockBlock.getAttribute("muted-option")).thenReturn("");
        when(mockBlock.getAttribute("controls-option")).thenReturn(null);

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(2, messages.size());
        
        // Check autoplay not allowed
        assertTrue(messages.stream().anyMatch(m -> 
                m.getMessage().contains("Video autoplay is not allowed") &&
                m.getSeverity() == Severity.WARN));
        
        // Check controls required
        assertTrue(messages.stream().anyMatch(m -> 
                m.getMessage().contains("Video controls are required") &&
                m.getSeverity() == Severity.ERROR));
    }

    @Test
    @DisplayName("should validate caption length")
    void shouldValidateCaptionLength() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.INFO)
                .caption(VideoBlock.CaptionConfig.builder()
                        .minLength(10)
                        .maxLength(50)
                        .severity(Severity.INFO)
                        .build())
                .build();

        when(mockBlock.getTitle()).thenReturn("Short");

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertEquals(1, messages.size());
        assertEquals(Severity.INFO, messages.get(0).getSeverity());
        assertTrue(messages.get(0).getMessage().contains("Video caption length 5 is less than minimum 10"));
    }

    @Test
    @DisplayName("should handle empty configuration")
    void shouldHandleEmptyConfiguration() {
        // Given
        VideoBlock config = VideoBlock.builder()
                .severity(Severity.WARN)
                .build();

        // When
        List<ValidationMessage> messages = validator.validate(mockBlock, config, context);

        // Then
        assertTrue(messages.isEmpty());
    }
}