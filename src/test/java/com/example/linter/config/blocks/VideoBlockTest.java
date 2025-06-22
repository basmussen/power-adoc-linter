package com.example.linter.config.blocks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;
import com.example.linter.config.rule.OccurrenceConfig;

@DisplayName("VideoBlock")
class VideoBlockTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build VideoBlock with all attributes")
        void shouldBuildVideoBlockWithAllAttributes() {
            // Given
            VideoBlock.UrlConfig urlConfig = VideoBlock.UrlConfig.builder()
                    .pattern("^https://.*\\.(mp4|webm|ogg)$")
                    .required(true)
                    .severity(Severity.ERROR)
                    .build();
                    
            VideoBlock.DimensionConfig widthConfig = VideoBlock.DimensionConfig.builder()
                    .minValue(320)
                    .maxValue(1920)
                    .required(false)
                    .severity(Severity.WARN)
                    .build();
                    
            VideoBlock.DimensionConfig heightConfig = VideoBlock.DimensionConfig.builder()
                    .minValue(240)
                    .maxValue(1080)
                    .required(false)
                    .severity(Severity.WARN)
                    .build();
                    
            VideoBlock.PosterConfig posterConfig = VideoBlock.PosterConfig.builder()
                    .required(true)
                    .pattern(".*\\.(jpg|jpeg|png)$")
                    .severity(Severity.ERROR)
                    .build();
                    
            VideoBlock.OptionsConfig optionsConfig = VideoBlock.OptionsConfig.builder()
                    .autoplay(VideoBlock.AutoplayConfig.builder()
                            .allowed(false)
                            .severity(Severity.ERROR)
                            .build())
                    .controls(VideoBlock.ControlsConfig.builder()
                            .required(true)
                            .severity(Severity.ERROR)
                            .build())
                    .build();
                    
            VideoBlock.CaptionConfig captionConfig = VideoBlock.CaptionConfig.builder()
                    .required(true)
                    .minLength(10)
                    .maxLength(200)
                    .severity(Severity.WARN)
                    .build();
            
            // When
            VideoBlock block = VideoBlock.builder()
                    .name("test-video")
                    .severity(Severity.ERROR)
                    .occurrence(OccurrenceConfig.builder()
                            .min(1)
                            .max(5)
                            .build())
                    .url(urlConfig)
                    .width(widthConfig)
                    .height(heightConfig)
                    .poster(posterConfig)
                    .options(optionsConfig)
                    .caption(captionConfig)
                    .build();
            
            // Then
            assertNotNull(block);
            assertEquals("test-video", block.getName());
            assertEquals(Severity.ERROR, block.getSeverity());
            assertNotNull(block.getOccurrence());
            assertEquals(1, block.getOccurrence().min());
            assertEquals(5, block.getOccurrence().max());
            
            assertNotNull(block.getUrl());
            assertTrue(block.getUrl().isRequired());
            assertEquals(Severity.ERROR, block.getUrl().getSeverity());
            
            assertNotNull(block.getWidth());
            assertEquals(320, block.getWidth().getMinValue());
            assertEquals(1920, block.getWidth().getMaxValue());
            
            assertNotNull(block.getOptions());
            assertNotNull(block.getOptions().getAutoplay());
            assertFalse(block.getOptions().getAutoplay().getAllowed());
            
            assertNotNull(block.getCaption());
            assertEquals(10, block.getCaption().getMinLength());
            assertEquals(200, block.getCaption().getMaxLength());
        }
        
        @Test
        @DisplayName("should build VideoBlock with minimal configuration")
        void shouldBuildVideoBlockWithMinimalConfiguration() {
            // When
            VideoBlock block = VideoBlock.builder()
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertNotNull(block);
            assertNull(block.getName());
            assertEquals(Severity.WARN, block.getSeverity());
            assertNull(block.getOccurrence());
            assertNull(block.getUrl());
            assertNull(block.getWidth());
            assertNull(block.getHeight());
            assertNull(block.getPoster());
            assertNull(block.getOptions());
            assertNull(block.getCaption());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When/Then
            assertThrows(NullPointerException.class, () -> {
                VideoBlock.builder().build();
            }, "severity is required");
        }
    }
    
    @Nested
    @DisplayName("UrlConfig Tests")
    class UrlConfigTests {
        
        @Test
        @DisplayName("should build UrlConfig with all fields")
        void shouldBuildUrlConfigWithAllFields() {
            // When
            VideoBlock.UrlConfig config = VideoBlock.UrlConfig.builder()
                    .required(true)
                    .pattern("^https://.*\\.(mp4|webm)$")
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertTrue(config.isRequired());
            assertNotNull(config.getPattern());
            assertEquals("^https://.*\\.(mp4|webm)$", config.getPattern().pattern());
            assertEquals(Severity.ERROR, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // When
            VideoBlock.UrlConfig config = VideoBlock.UrlConfig.builder()
                    .pattern(null)
                    .build();
            
            // Then
            assertNull(config.getPattern());
        }
        
        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            VideoBlock.UrlConfig config1 = VideoBlock.UrlConfig.builder()
                    .required(true)
                    .pattern("^https://.*")
                    .severity(Severity.ERROR)
                    .build();
                    
            VideoBlock.UrlConfig config2 = VideoBlock.UrlConfig.builder()
                    .required(true)
                    .pattern("^https://.*")
                    .severity(Severity.ERROR)
                    .build();
                    
            VideoBlock.UrlConfig config3 = VideoBlock.UrlConfig.builder()
                    .required(false)
                    .pattern("^https://.*")
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("DimensionConfig Tests")
    class DimensionConfigTests {
        
        @Test
        @DisplayName("should build DimensionConfig with all fields")
        void shouldBuildDimensionConfigWithAllFields() {
            // When
            VideoBlock.DimensionConfig config = VideoBlock.DimensionConfig.builder()
                    .required(true)
                    .minValue(100)
                    .maxValue(1000)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertTrue(config.isRequired());
            assertEquals(100, config.getMinValue());
            assertEquals(1000, config.getMaxValue());
            assertEquals(Severity.WARN, config.getSeverity());
        }
        
        @Test
        @DisplayName("should handle null values")
        void shouldHandleNullValues() {
            // When
            VideoBlock.DimensionConfig config = VideoBlock.DimensionConfig.builder()
                    .build();
            
            // Then
            assertFalse(config.isRequired());
            assertNull(config.getMinValue());
            assertNull(config.getMaxValue());
            assertNull(config.getSeverity());
        }
    }
    
    @Nested
    @DisplayName("OptionsConfig Tests")
    class OptionsConfigTests {
        
        @Test
        @DisplayName("should build OptionsConfig with nested configs")
        void shouldBuildOptionsConfigWithNestedConfigs() {
            // When
            VideoBlock.OptionsConfig config = VideoBlock.OptionsConfig.builder()
                    .autoplay(VideoBlock.AutoplayConfig.builder()
                            .allowed(false)
                            .severity(Severity.ERROR)
                            .build())
                    .controls(VideoBlock.ControlsConfig.builder()
                            .required(true)
                            .severity(Severity.ERROR)
                            .build())
                    .build();
            
            // Then
            assertNotNull(config.getAutoplay());
            assertFalse(config.getAutoplay().getAllowed());
            assertEquals(Severity.ERROR, config.getAutoplay().getSeverity());
            
            assertNotNull(config.getControls());
            assertTrue(config.getControls().isRequired());
            assertEquals(Severity.ERROR, config.getControls().getSeverity());
        }
        
        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            VideoBlock.AutoplayConfig autoplay = VideoBlock.AutoplayConfig.builder()
                    .allowed(false)
                    .build();
                    
            VideoBlock.ControlsConfig controls = VideoBlock.ControlsConfig.builder()
                    .required(true)
                    .build();
                    
            VideoBlock.OptionsConfig config1 = VideoBlock.OptionsConfig.builder()
                    .autoplay(autoplay)
                    .controls(controls)
                    .build();
                    
            VideoBlock.OptionsConfig config2 = VideoBlock.OptionsConfig.builder()
                    .autoplay(autoplay)
                    .controls(controls)
                    .build();
                    
            VideoBlock.OptionsConfig config3 = VideoBlock.OptionsConfig.builder()
                    .autoplay(autoplay)
                    .build();
            
            // Then
            assertEquals(config1, config2);
            assertEquals(config1.hashCode(), config2.hashCode());
            assertNotEquals(config1, config3);
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsAndHashCodeTests {
        
        @Test
        @DisplayName("should correctly implement equals")
        void shouldCorrectlyImplementEquals() {
            // Given
            VideoBlock block1 = VideoBlock.builder()
                    .name("video1")
                    .severity(Severity.ERROR)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .build())
                    .build();
                    
            VideoBlock block2 = VideoBlock.builder()
                    .name("video1")
                    .severity(Severity.ERROR)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .build())
                    .build();
                    
            VideoBlock block3 = VideoBlock.builder()
                    .name("video2")
                    .severity(Severity.ERROR)
                    .url(VideoBlock.UrlConfig.builder()
                            .required(true)
                            .build())
                    .build();
            
            // Then
            assertEquals(block1, block1); // reflexive
            assertEquals(block1, block2); // symmetric
            assertEquals(block2, block1);
            assertNotEquals(block1, block3);
            assertNotEquals(block1, null);
            assertNotEquals(block1, "not a VideoBlock");
        }
        
        @Test
        @DisplayName("should correctly implement hashCode")
        void shouldCorrectlyImplementHashCode() {
            // Given
            VideoBlock block1 = VideoBlock.builder()
                    .name("video1")
                    .severity(Severity.ERROR)
                    .build();
                    
            VideoBlock block2 = VideoBlock.builder()
                    .name("video1")
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertEquals(block1.hashCode(), block2.hashCode());
        }
    }
    
    @Nested
    @DisplayName("Pattern Handling Tests")
    class PatternHandlingTests {
        
        @Test
        @DisplayName("should handle Pattern objects in equals correctly")
        void shouldHandlePatternObjectsInEqualsCorrectly() {
            // Given
            String patternString = "^https://.*\\.(mp4|webm)$";
            
            VideoBlock.UrlConfig config1 = VideoBlock.UrlConfig.builder()
                    .pattern(patternString)
                    .build();
                    
            VideoBlock.UrlConfig config2 = VideoBlock.UrlConfig.builder()
                    .pattern(patternString)
                    .build();
                    
            VideoBlock.UrlConfig config3 = VideoBlock.UrlConfig.builder()
                    .pattern("^http://.*")
                    .build();
            
            // Then
            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
        }
        
        @Test
        @DisplayName("should handle null patterns in equals")
        void shouldHandleNullPatternsInEquals() {
            // Given
            VideoBlock.UrlConfig config1 = VideoBlock.UrlConfig.builder()
                    .pattern(null)
                    .build();
                    
            VideoBlock.UrlConfig config2 = VideoBlock.UrlConfig.builder()
                    .pattern(null)
                    .build();
                    
            VideoBlock.UrlConfig config3 = VideoBlock.UrlConfig.builder()
                    .pattern("^https://.*")
                    .build();
            
            // Then
            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
            assertNotEquals(config3, config1);
        }
    }
}