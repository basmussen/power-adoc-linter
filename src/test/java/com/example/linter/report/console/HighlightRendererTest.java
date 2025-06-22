package com.example.linter.report.console;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;
import com.example.linter.config.output.DisplayConfig;
import com.example.linter.config.output.HighlightStyle;
import com.example.linter.validator.*;

/**
 * Tests for HighlightRenderer.
 */
@DisplayName("HighlightRenderer Tests")
class HighlightRendererTest {
    
    private HighlightRenderer renderer;
    private ColorScheme colorScheme;
    
    @BeforeEach
    void setUp() {
        DisplayConfig config = DisplayConfig.builder()
            .highlightStyle(HighlightStyle.UNDERLINE)
            .useColors(false) // Disable colors for easier testing
            .build();
        colorScheme = new ColorScheme(false);
        renderer = new HighlightRenderer(config, colorScheme);
    }
    
    @Nested
    @DisplayName("Underline Style Tests")
    class UnderlineStyleTests {
        
        @Test
        @DisplayName("should highlight error at specific column")
        void shouldHighlightErrorAtSpecificColumn() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Invalid value")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(10)
                    .length(5)
                    .build())
                .contextLines(Arrays.asList("This is a test line with error"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            String[] lines = result.split("\n");
            assertEquals(2, lines.length);
            assertEquals("This is a test line with error", lines[0]);
            assertEquals("         ~~~~~", lines[1]); // Underline at column 10, length 5
        }
        
        @Test
        @DisplayName("should highlight entire line when no column specified")
        void shouldHighlightEntireLineWhenNoColumnSpecified() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Line too long")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(0)
                    .build())
                .contextLines(Arrays.asList("Short line"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            String[] lines = result.split("\n");
            assertEquals(2, lines.length);
            assertEquals("Short line", lines[0]);
            assertEquals("~~~~~~~~~~", lines[1]); // Entire line underlined
        }
        
        @Test
        @DisplayName("should insert placeholder for missing value")
        void shouldInsertPlaceholderForMissingValue() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Missing attribute")
                .missingValueHint("id")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(8)
                    .insertionPoint(true)
                    .build())
                .contextLines(Arrays.asList("[source,java]"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertTrue(result.contains("[source,«id»java]"));
            assertTrue(result.contains("~~~~~")); // Underline under placeholder
        }
    }
    
    @Nested
    @DisplayName("Box Style Tests")
    class BoxStyleTests {
        
        @BeforeEach
        void setUp() {
            DisplayConfig config = DisplayConfig.builder()
                .highlightStyle(HighlightStyle.BOX)
                .useColors(false)
                .build();
            renderer = new HighlightRenderer(config, colorScheme);
        }
        
        @Test
        @DisplayName("should render box style highlighting")
        void shouldRenderBoxStyleHighlighting() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Invalid value")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(5)
                    .length(7)
                    .build())
                .contextLines(Arrays.asList("Test invalid text"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertTrue(result.contains("│ Test invalid text │"));
            assertTrue(result.contains("└─────┘")); // Box under "invalid"
        }
    }
    
    @Nested
    @DisplayName("Arrow Style Tests")
    class ArrowStyleTests {
        
        @BeforeEach
        void setUp() {
            DisplayConfig config = DisplayConfig.builder()
                .highlightStyle(HighlightStyle.ARROW)
                .useColors(false)
                .build();
            renderer = new HighlightRenderer(config, colorScheme);
        }
        
        @Test
        @DisplayName("should render arrow style highlighting")
        void shouldRenderArrowStyleHighlighting() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Error here")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(10)
                    .build())
                .contextLines(Arrays.asList("Some text here"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertTrue(result.contains("→ Some text here"));
            assertTrue(result.contains("          ^")); // Arrow pointing to column 10
        }
    }
    
    @Nested
    @DisplayName("None Style Tests")
    class NoneStyleTests {
        
        @BeforeEach
        void setUp() {
            DisplayConfig config = DisplayConfig.builder()
                .highlightStyle(HighlightStyle.NONE)
                .useColors(false)
                .build();
            renderer = new HighlightRenderer(config, colorScheme);
        }
        
        @Test
        @DisplayName("should not add highlighting with none style")
        void shouldNotAddHighlightingWithNoneStyle() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Error")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(5)
                    .build())
                .contextLines(Arrays.asList("Test line"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertEquals("Test line", result.trim());
            assertFalse(result.contains("~"));
            assertFalse(result.contains("^"));
            assertFalse(result.contains("│"));
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle empty context lines")
        void shouldHandleEmptyContextLines() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Error")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .build())
                .contextLines(Arrays.asList())
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertTrue(result.isEmpty());
        }
        
        @Test
        @DisplayName("should handle column beyond line length")
        void shouldHandleColumnBeyondLineLength() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .message("Error")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(1)
                    .column(50) // Beyond line length
                    .build())
                .contextLines(Arrays.asList("Short"))
                .build();
            
            // When
            String result = renderer.renderHighlight(message, 0);
            
            // Then
            assertNotNull(result);
            assertTrue(result.contains("Short"));
        }
    }
}