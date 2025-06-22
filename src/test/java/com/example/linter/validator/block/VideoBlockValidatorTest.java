package com.example.linter.validator.block;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.blocks.VideoBlock;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@DisplayName("VideoBlockValidator")
class VideoBlockValidatorTest {
    
    private VideoBlockValidator validator;
    
    @Mock
    private StructuralNode node;
    
    @Mock
    private org.asciidoctor.ast.Cursor sourceLocation;
    
    @Mock
    private BlockValidationContext context;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new VideoBlockValidator();
        when(node.getSourceLocation()).thenReturn(sourceLocation);
        when(sourceLocation.getLineNumber()).thenReturn(10);
        when(sourceLocation.getFile()).thenReturn("test.adoc");
        
        // Mock context.createLocation()
        SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .startLine(10)
                .build();
        when(context.createLocation(any(StructuralNode.class))).thenReturn(location);
    }
    
    @Test
    @DisplayName("should return VIDEO block type")
    void shouldReturnVideoBlockType() {
        assertEquals(BlockType.VIDEO, validator.getSupportedType());
    }
    
    @Test
    @DisplayName("should throw exception for non-VideoBlock config")
    void shouldThrowForNonVideoBlockConfig() {
        assertThrows(IllegalArgumentException.class, () ->
                validator.validate(node, ParagraphBlock.builder().name("test").severity(Severity.WARN).build(), context));
    }
    
    @Nested
    @DisplayName("URL Validation")
    class UrlValidation {
        
        @Test
        @DisplayName("should report error when required URL is missing")
        void shouldReportErrorWhenRequiredUrlMissing() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .severity(Severity.ERROR)
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
            assertEquals("Video URL is required but not provided", messages.get(0).getMessage());
        }
        
        @Test
        @DisplayName("should report error when URL doesn't match pattern")
        void shouldReportErrorWhenUrlDoesntMatchPattern() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .pattern("^https?://.*\\.(mp4|webm)$")
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn("https://example.com/video.avi");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
            assertTrue(messages.get(0).getMessage().contains("does not match required pattern"));
        }
        
        @Test
        @DisplayName("should use nested severity when available")
        void shouldUseNestedSeverityWhenAvailable() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .severity(Severity.INFO)
                            .build())
                    .build();
            
            when(node.getAttribute("target")).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }
    }
    
    @Nested
    @DisplayName("Dimension Validation")
    class DimensionValidation {
        
        @Test
        @DisplayName("should report error when width is below minimum")
        void shouldReportErrorWhenWidthBelowMinimum() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .maxValue(1920)
                            .build())
                    .build();
            
            when(node.getAttribute("width")).thenReturn("200");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals("Video width 200 is below minimum value 320", messages.get(0).getMessage());
        }
        
        @Test
        @DisplayName("should report error when height exceeds maximum")
        void shouldReportErrorWhenHeightExceedsMaximum() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.ERROR)
                    .height(VideoBlock.DimensionConfig.builder()
                            .maxValue(1080)
                            .severity(Severity.INFO)
                            .build())
                    .build();
            
            when(node.getAttribute("height")).thenReturn("2000");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
            assertEquals("Video height 2000 exceeds maximum value 1080", messages.get(0).getMessage());
        }
        
        @Test
        @DisplayName("should report error for invalid dimension value")
        void shouldReportErrorForInvalidDimensionValue() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.ERROR)
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .build())
                    .build();
            
            when(node.getAttribute("width")).thenReturn("invalid");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals("Video width 'invalid' is not a valid number", messages.get(0).getMessage());
        }
    }
    
    @Nested
    @DisplayName("Poster Validation")
    class PosterValidation {
        
        @Test
        @DisplayName("should report error when poster doesn't match pattern")
        void shouldReportErrorWhenPosterDoesntMatchPattern() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .poster(VideoBlock.PosterConfig.builder()
                            .pattern(".*\\.(jpg|jpeg|png)$")
                            .build())
                    .build();
            
            when(node.getAttribute("poster")).thenReturn("poster.gif");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("Video poster 'poster.gif' does not match"));
        }
    }
    
    @Nested
    @DisplayName("Controls Validation")
    class ControlsValidation {
        
        @Test
        @DisplayName("should report error when controls are required but not enabled")
        void shouldReportErrorWhenControlsRequiredButNotEnabled() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .options(VideoBlock.OptionsConfig.builder()
                            .controls(VideoBlock.ControlsConfig.builder()
                                    .required(true)
                                    .severity(Severity.ERROR)
                                    .build())
                            .build())
                    .build();
            
            when(node.getAttribute("options")).thenReturn("autoplay");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
            assertEquals("Video controls are required but not enabled", messages.get(0).getMessage());
        }
        
        @Test
        @DisplayName("should pass when controls are enabled")
        void shouldPassWhenControlsAreEnabled() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .options(VideoBlock.OptionsConfig.builder()
                            .controls(VideoBlock.ControlsConfig.builder()
                                    .required(true)
                                    .build())
                            .build())
                    .build();
            
            when(node.getAttribute("options")).thenReturn("controls,loop");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Caption Validation")
    class CaptionValidation {
        
        @Test
        @DisplayName("should report error when caption is too short")
        void shouldReportErrorWhenCaptionTooShort() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .minLength(15)
                            .severity(Severity.WARN)
                            .build())
                    .build();
            
            when(node.getAttribute("caption")).thenReturn("Short");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
            assertEquals("Video caption length 5 is below minimum 15", messages.get(0).getMessage());
        }
        
        @Test
        @DisplayName("should report error when caption is too long")
        void shouldReportErrorWhenCaptionTooLong() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .maxLength(50)
                            .build())
                    .build();
            
            String longCaption = "This is a very long caption that definitely exceeds the maximum allowed length";
            when(node.getAttribute("caption")).thenReturn(longCaption);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getMessage().contains("exceeds maximum"));
        }
        
        @Test
        @DisplayName("should use title when caption attribute is null")
        void shouldUseTitleWhenCaptionAttributeIsNull() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .caption(VideoBlock.CaptionConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            when(node.getAttribute("caption")).thenReturn(null);
            when(node.getTitle()).thenReturn("Video Title");
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Complex Scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate all configured rules")
        void shouldValidateAllConfiguredRules() {
            VideoBlock config = VideoBlock.builder()
                    .name("test")
                    .severity(Severity.WARN)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .pattern("^https://.*\\.(mp4|webm)$")
                            .build())
                    .width(VideoBlock.DimensionConfig.builder()
                            .minValue(320)
                            .maxValue(1920)
                            .build())
                    .caption(VideoBlock.CaptionConfig.builder()
                            .required(true)
                            .minLength(10)
                            .build())
                    .build();
            
            // Invalid URL
            when(node.getAttribute("target")).thenReturn("http://example.com/video.avi");
            // Valid width
            when(node.getAttribute("width")).thenReturn("800");
            // Missing caption
            when(node.getAttribute("caption")).thenReturn(null);
            when(node.getTitle()).thenReturn(null);
            
            List<ValidationMessage> messages = validator.validate(node, config, context);
            
            assertEquals(2, messages.size());
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("URL")));
            assertTrue(messages.stream().anyMatch(m -> m.getMessage().contains("caption")));
        }
    }
}