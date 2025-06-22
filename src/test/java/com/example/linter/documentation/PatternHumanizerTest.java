package com.example.linter.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("PatternHumanizer")
class PatternHumanizerTest {
    
    private PatternHumanizer humanizer;
    
    @BeforeEach
    void setUp() {
        humanizer = new PatternHumanizer();
    }
    
    @Nested
    @DisplayName("Known Patterns")
    class KnownPatterns {
        
        @Test
        @DisplayName("should humanize uppercase start pattern")
        void shouldHumanizeUppercaseStartPattern() {
            String result = humanizer.humanize("^[A-Z].*");
            assertEquals("Muss mit einem Großbuchstaben beginnen", result);
        }
        
        @Test
        @DisplayName("should humanize semantic versioning pattern")
        void shouldHumanizeSemanticVersioningPattern() {
            String result = humanizer.humanize("^\\d+\\.\\d+\\.\\d+$");
            assertEquals("Semantic Versioning Format (z.B. 1.0.0)", result);
        }
        
        @Test
        @DisplayName("should humanize email pattern")
        void shouldHumanizeEmailPattern() {
            String result = humanizer.humanize("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");
            assertEquals("Gültige E-Mail-Adresse", result);
        }
        
        @Test
        @DisplayName("should humanize image file pattern")
        void shouldHumanizeImageFilePattern() {
            String result = humanizer.humanize(".*\\.(png|jpg|jpeg|gif|svg)$");
            assertEquals("Bilddatei (PNG, JPG, JPEG, GIF oder SVG)", result);
        }
        
        @Test
        @DisplayName("should humanize audio file pattern")
        void shouldHumanizeAudioFilePattern() {
            String result = humanizer.humanize(".*\\.(mp3|ogg|wav|m4a)$");
            assertEquals("Audiodatei (MP3, OGG, WAV oder M4A)", result);
        }
    }
    
    @Nested
    @DisplayName("Generated Descriptions")
    class GeneratedDescriptions {
        
        @Test
        @DisplayName("should generate description for simple starts-with pattern")
        void shouldGenerateStartsWithDescription() {
            String result = humanizer.humanize("^Chapter");
            assertEquals("Muss mit 'Chapter' beginnen", result);
        }
        
        @Test
        @DisplayName("should generate description for simple ends-with pattern")
        void shouldGenerateEndsWithDescription() {
            String result = humanizer.humanize("END$");
            assertEquals("Muss mit 'END' enden", result);
        }
        
        @Test
        @DisplayName("should generate description for exact match pattern")
        void shouldGenerateExactMatchDescription() {
            String result = humanizer.humanize("^EXACT$");
            assertEquals("Muss genau 'EXACT' sein", result);
        }
        
        @Test
        @DisplayName("should generate description for letters only pattern")
        void shouldGenerateLettersOnlyDescription() {
            String result = humanizer.humanize("^[A-Za-z]+$");
            assertEquals("Nur Buchstaben erlaubt", result);
        }
        
        @Test
        @DisplayName("should generate description for numbers only pattern")
        void shouldGenerateNumbersOnlyDescription() {
            String result = humanizer.humanize("^[0-9]+$");
            assertEquals("Nur Zahlen erlaubt", result);
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle null pattern")
        void shouldHandleNullPattern() {
            String result = humanizer.humanize((Pattern) null);
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("should handle empty pattern string")
        void shouldHandleEmptyPatternString() {
            String result = humanizer.humanize("");
            assertEquals("", result);
        }
        
        @Test
        @DisplayName("should show pattern for complex regex")
        void shouldShowPatternForComplexRegex() {
            String complex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$";
            String result = humanizer.humanize(complex);
            assertEquals("Muss dem Muster entsprechen: " + complex, result);
        }
    }
    
    @Nested
    @DisplayName("Custom Patterns")
    class CustomPatterns {
        
        @Test
        @DisplayName("should use registered custom pattern")
        void shouldUseRegisteredCustomPattern() {
            // Given
            String pattern = "^CUSTOM-\\d{4}$";
            String description = "Muss dem Format CUSTOM-XXXX entsprechen (4 Ziffern)";
            
            // When
            humanizer.registerPattern(pattern, description);
            String result = humanizer.humanize(pattern);
            
            // Then
            assertEquals(description, result);
        }
    }
}