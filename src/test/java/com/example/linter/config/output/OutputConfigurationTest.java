package com.example.linter.config.output;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for OutputConfiguration and related classes.
 */
@DisplayName("OutputConfiguration Tests")
class OutputConfigurationTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should create configuration with all fields")
        void shouldCreateConfigurationWithAllFields() {
            // Given
            DisplayConfig display = DisplayConfig.builder()
                .useColors(true)
                .contextLines(3)
                .showLineNumbers(true)
                .highlightStyle(HighlightStyle.UNDERLINE)
                .build();
                
            ErrorGroupingConfig grouping = ErrorGroupingConfig.builder()
                .enabled(true)
                .build();
                
            SummaryConfig summary = SummaryConfig.builder()
                .enabled(true)
                .showStatistics(true)
                .showMostCommon(true)
                .showFileList(true)
                .build();
            
            // When
            OutputConfiguration config = OutputConfiguration.builder()
                .format(OutputFormat.ENHANCED)
                .display(display)
                .errorGrouping(grouping)
                .summary(summary)
                .build();
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertEquals(display, config.getDisplay());
            assertEquals(grouping, config.getErrorGrouping());
            assertEquals(summary, config.getSummary());
        }
        
        @Test
        @DisplayName("should use default values when not specified")
        void shouldUseDefaultValuesWhenNotSpecified() {
            // When
            OutputConfiguration config = OutputConfiguration.builder().build();
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertNotNull(config.getDisplay());
            assertNotNull(config.getErrorGrouping());
            assertNotNull(config.getSummary());
        }
        
        @Test
        @DisplayName("should require non-null format")
        void shouldRequireNonNullFormat() {
            // When/Then
            assertThrows(NullPointerException.class, () ->
                OutputConfiguration.builder()
                    .format(null)
                    .build()
            );
        }
    }
    
    @Nested
    @DisplayName("Default Configuration Tests")
    class DefaultConfigurationTests {
        
        @Test
        @DisplayName("should create default enhanced configuration")
        void shouldCreateDefaultEnhancedConfiguration() {
            // When
            OutputConfiguration config = OutputConfiguration.defaultConfig();
            
            // Then
            assertEquals(OutputFormat.ENHANCED, config.getFormat());
            assertTrue(config.getDisplay().isUseColors());
            assertEquals(3, config.getDisplay().getContextLines());
            assertTrue(config.getDisplay().isShowLineNumbers());
            assertEquals(HighlightStyle.UNDERLINE, config.getDisplay().getHighlightStyle());
            
            assertTrue(config.getErrorGrouping().isEnabled());
            assertTrue(config.getSummary().isEnabled());
        }
        
        @Test
        @DisplayName("should create compact configuration")
        void shouldCreateCompactConfiguration() {
            // When
            OutputConfiguration config = OutputConfiguration.compactConfig();
            
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
    @DisplayName("DisplayConfig Tests")
    class DisplayConfigTests {
        
        @Test
        @DisplayName("should validate context lines range")
        void shouldValidateContextLinesRange() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                DisplayConfig.builder()
                    .contextLines(-1)
                    .build()
            );
            
            assertThrows(IllegalArgumentException.class, () ->
                DisplayConfig.builder()
                    .contextLines(11)
                    .build()
            );
        }
        
        @Test
        @DisplayName("should accept valid context lines")
        void shouldAcceptValidContextLines() {
            // When
            DisplayConfig config0 = DisplayConfig.builder().contextLines(0).build();
            DisplayConfig config5 = DisplayConfig.builder().contextLines(5).build();
            DisplayConfig config10 = DisplayConfig.builder().contextLines(10).build();
            
            // Then
            assertEquals(0, config0.getContextLines());
            assertEquals(5, config5.getContextLines());
            assertEquals(10, config10.getContextLines());
        }
    }
    
    @Nested
    @DisplayName("ErrorGroupingConfig Tests")
    class ErrorGroupingConfigTests {
        
        @Test
        @DisplayName("should validate max group size")
        void shouldValidateMaxGroupSize() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                ErrorGroupingConfig.builder()
                    .threshold(0)
                    .build()
            );
        }
        
        @Test
        @DisplayName("should validate threshold")
        void shouldValidateThreshold() {
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                ErrorGroupingConfig.builder()
                    .threshold(-1)
                    .build()
            );
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("should implement equals correctly")
        void shouldImplementEqualsCorrectly() {
            // Given
            OutputConfiguration config1 = OutputConfiguration.defaultConfig();
            OutputConfiguration config2 = OutputConfiguration.defaultConfig();
            OutputConfiguration config3 = OutputConfiguration.compactConfig();
            
            // Then
            assertEquals(config1, config1);
            assertEquals(config1, config2);
            assertNotEquals(config1, config3);
            assertNotEquals(config1, null);
            assertNotEquals(config1, "not a config");
        }
        
        @Test
        @DisplayName("should implement hashCode correctly")
        void shouldImplementHashCodeCorrectly() {
            // Given
            OutputConfiguration config1 = OutputConfiguration.defaultConfig();
            OutputConfiguration config2 = OutputConfiguration.defaultConfig();
            
            // Then
            assertEquals(config1.hashCode(), config2.hashCode());
        }
    }
}