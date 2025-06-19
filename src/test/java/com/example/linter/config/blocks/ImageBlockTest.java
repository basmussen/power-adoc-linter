package com.example.linter.config.blocks;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ImageBlock")
class ImageBlockTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build ImageBlock with all attributes")
        void shouldBuildImageBlockWithAllAttributes() {
            // Given
            ImageBlock.UrlRule urlRule = ImageBlock.UrlRule.builder()
                    .pattern("^https?://.*\\.(jpg|jpeg|png|gif|svg)$")
                    .required(true)
                    .build();
                    
            ImageBlock.DimensionRule heightRule = ImageBlock.DimensionRule.builder()
                    .minValue(100)
                    .maxValue(2000)
                    .required(false)
                    .build();
                    
            ImageBlock.DimensionRule widthRule = ImageBlock.DimensionRule.builder()
                    .minValue(100)
                    .maxValue(3000)
                    .required(false)
                    .build();
                    
            ImageBlock.AltTextRule altRule = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(10)
                    .maxLength(200)
                    .build();
            
            // When
            ImageBlock image = ImageBlock.builder()
                    .severity(Severity.ERROR)
                    .url(urlRule)
                    .height(heightRule)
                    .width(widthRule)
                    .alt(altRule)
                    .build();
            
            // Then
            assertEquals(Severity.ERROR, image.getSeverity());
            
            assertNotNull(image.getUrl());
            assertTrue(image.getUrl().isRequired());
            assertNotNull(image.getUrl().getPattern());
            
            assertNotNull(image.getHeight());
            assertFalse(image.getHeight().isRequired());
            assertEquals(100, image.getHeight().getMinValue());
            assertEquals(2000, image.getHeight().getMaxValue());
            
            assertNotNull(image.getWidth());
            assertFalse(image.getWidth().isRequired());
            assertEquals(100, image.getWidth().getMinValue());
            assertEquals(3000, image.getWidth().getMaxValue());
            
            assertNotNull(image.getAlt());
            assertTrue(image.getAlt().isRequired());
            assertEquals(10, image.getAlt().getMinLength());
            assertEquals(200, image.getAlt().getMaxLength());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ImageBlock.builder().build();
            });
        }
    }
    
    @Nested
    @DisplayName("UrlRule Tests")
    class UrlRuleTests {
        
        @Test
        @DisplayName("should create UrlRule with string pattern")
        void shouldCreateUrlRuleWithStringPattern() {
            // Given & When
            ImageBlock.UrlRule urlRule = ImageBlock.UrlRule.builder()
                    .pattern("^https://.*")
                    .required(true)
                    .build();
            
            // Then
            assertNotNull(urlRule.getPattern());
            assertEquals("^https://.*", urlRule.getPattern().pattern());
            assertTrue(urlRule.isRequired());
        }
        
        @Test
        @DisplayName("should create UrlRule with Pattern object")
        void shouldCreateUrlRuleWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile(".*\\.png$");
            
            // When
            ImageBlock.UrlRule urlRule = ImageBlock.UrlRule.builder()
                    .pattern(pattern)
                    .required(false)
                    .build();
            
            // Then
            assertEquals(pattern, urlRule.getPattern());
            assertFalse(urlRule.isRequired());
        }
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            // Given & When
            ImageBlock.UrlRule urlRule = ImageBlock.UrlRule.builder()
                    .pattern((String) null)
                    .build();
            
            // Then
            assertNull(urlRule.getPattern());
        }
    }
    
    @Nested
    @DisplayName("DimensionRule Tests")
    class DimensionRuleTests {
        
        @Test
        @DisplayName("should create DimensionRule with min and max values")
        void shouldCreateDimensionRuleWithMinAndMaxValues() {
            // Given & When
            ImageBlock.DimensionRule dimension = ImageBlock.DimensionRule.builder()
                    .minValue(50)
                    .maxValue(1000)
                    .required(true)
                    .build();
            
            // Then
            assertEquals(50, dimension.getMinValue());
            assertEquals(1000, dimension.getMaxValue());
            assertTrue(dimension.isRequired());
        }
        
        @Test
        @DisplayName("should allow optional dimensions")
        void shouldAllowOptionalDimensions() {
            // Given & When
            ImageBlock.DimensionRule dimension = ImageBlock.DimensionRule.builder()
                    .required(false)
                    .build();
            
            // Then
            assertNull(dimension.getMinValue());
            assertNull(dimension.getMaxValue());
            assertFalse(dimension.isRequired());
        }
    }
    
    @Nested
    @DisplayName("AltTextRule Tests")
    class AltTextRuleTests {
        
        @Test
        @DisplayName("should create AltTextRule with length constraints")
        void shouldCreateAltTextRuleWithLengthConstraints() {
            // Given & When
            ImageBlock.AltTextRule altText = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(150)
                    .build();
            
            // Then
            assertTrue(altText.isRequired());
            assertEquals(5, altText.getMinLength());
            assertEquals(150, altText.getMaxLength());
        }
        
        @Test
        @DisplayName("should allow optional alt text")
        void shouldAllowOptionalAltText() {
            // Given & When
            ImageBlock.AltTextRule altText = ImageBlock.AltTextRule.builder()
                    .required(false)
                    .build();
            
            // Then
            assertFalse(altText.isRequired());
            assertNull(altText.getMinLength());
            assertNull(altText.getMaxLength());
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            ImageBlock.UrlRule url1 = ImageBlock.UrlRule.builder()
                    .pattern(".*\\.jpg$")
                    .required(true)
                    .build();
                    
            ImageBlock.UrlRule url2 = ImageBlock.UrlRule.builder()
                    .pattern(".*\\.jpg$")
                    .required(true)
                    .build();
                    
            ImageBlock.AltTextRule alt1 = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(10)
                    .build();
                    
            ImageBlock.AltTextRule alt2 = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(10)
                    .build();
                    
            // When
            ImageBlock image1 = ImageBlock.builder()
                    .severity(Severity.WARN)
                    .url(url1)
                    .alt(alt1)
                    .build();
                    
            ImageBlock image2 = ImageBlock.builder()
                    .severity(Severity.WARN)
                    .url(url2)
                    .alt(alt2)
                    .build();
                    
            ImageBlock image3 = ImageBlock.builder()
                    .severity(Severity.ERROR)
                    .url(url1)
                    .alt(alt1)
                    .build();
            
            // Then
            assertEquals(image1, image2);
            assertNotEquals(image1, image3);
            assertEquals(image1.hashCode(), image2.hashCode());
            assertNotEquals(image1.hashCode(), image3.hashCode());
        }
        
        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            ImageBlock.UrlRule url1 = ImageBlock.UrlRule.builder()
                    .pattern("test")
                    .required(true)
                    .build();
                    
            ImageBlock.UrlRule url2 = ImageBlock.UrlRule.builder()
                    .pattern("test")
                    .required(true)
                    .build();
                    
            ImageBlock.DimensionRule dim1 = ImageBlock.DimensionRule.builder()
                    .minValue(100)
                    .maxValue(200)
                    .required(false)
                    .build();
                    
            ImageBlock.DimensionRule dim2 = ImageBlock.DimensionRule.builder()
                    .minValue(100)
                    .maxValue(200)
                    .required(false)
                    .build();
                    
            ImageBlock.AltTextRule alt1 = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .build();
                    
            ImageBlock.AltTextRule alt2 = ImageBlock.AltTextRule.builder()
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .build();
            
            // Then
            assertEquals(url1, url2);
            assertEquals(url1.hashCode(), url2.hashCode());
            
            assertEquals(dim1, dim2);
            assertEquals(dim1.hashCode(), dim2.hashCode());
            
            assertEquals(alt1, alt2);
            assertEquals(alt1.hashCode(), alt2.hashCode());
        }
    }
}