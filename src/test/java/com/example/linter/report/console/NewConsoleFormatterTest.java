package com.example.linter.report.console;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;
import com.example.linter.config.output.*;
import com.example.linter.report.ConsoleFormatter;
import com.example.linter.validator.*;

/**
 * Tests for the new enhanced ConsoleFormatter.
 */
@DisplayName("New ConsoleFormatter Tests")
class NewConsoleFormatterTest {
    
    private StringWriter output;
    private PrintWriter writer;
    
    @BeforeEach
    void setUp() {
        output = new StringWriter();
        writer = new PrintWriter(output);
    }
    
    @Nested
    @DisplayName("Enhanced Format Tests")
    class EnhancedFormatTests {
        
        @Test
        @DisplayName("should render enhanced format with context and highlighting")
        void shouldRenderEnhancedFormatWithContextAndHighlighting() {
            // Given
            OutputConfiguration config = OutputConfiguration.defaultConfig();
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("paragraph.line-count")
                .message("Paragraph has too many lines")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(10)
                    .column(1)
                    .build())
                .actualValue("15")
                .expectedValue("< 10")
                .contextLines(Arrays.asList(
                    "Line 8: Some context before",
                    "Line 9: More context",
                    "Line 10: This is the problematic paragraph with too many lines",
                    "Line 11: Context after",
                    "Line 12: More context after"
                ))
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("test.adoc:10:1"));
            assertTrue(output.contains("ERROR"));
            assertTrue(output.contains("paragraph.line-count"));
            assertTrue(output.contains("Paragraph has too many lines"));
            assertTrue(output.contains("Line 8: Some context before"));
            assertTrue(output.contains("Line 10: This is the problematic paragraph"));
            assertTrue(output.contains("Expected: < 10"));
            assertTrue(output.contains("Actual: 15"));
        }
        
        @Test
        @DisplayName("should render suggestions when available")
        void shouldRenderSuggestionsWhenAvailable() {
            // Given
            OutputConfiguration config = OutputConfiguration.defaultConfig();
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            Suggestion suggestion = Suggestion.builder()
                .description("Split paragraph into smaller ones")
                .example("Paragraph 1...\n\nParagraph 2...")
                .isAutoFixable(true)
                .build();
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("paragraph.line-count")
                .message("Paragraph has too many lines")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(10)
                    .column(1)
                    .build())
                .suggestions(Arrays.asList(suggestion))
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("Suggestions:"));
            assertTrue(output.contains("Split paragraph into smaller ones"));
            assertTrue(output.contains("Paragraph 1..."));
            assertTrue(output.contains("(auto-fixable)"));
        }
    }
    
    @Nested
    @DisplayName("Simple Format Tests")
    class SimpleFormatTests {
        
        @Test
        @DisplayName("should render simple format without context")
        void shouldRenderSimpleFormatWithoutContext() {
            // Given
            OutputConfiguration config = OutputConfiguration.builder()
                .format(OutputFormat.SIMPLE)
                .display(DisplayConfig.builder()
                    .useColors(false)
                    .contextLines(0)
                    .highlightStyle(HighlightStyle.NONE)
                    .build())
                .build();
            
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("paragraph.line-count")
                .message("Paragraph has too many lines")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(10)
                    .column(1)
                    .build())
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("test.adoc:10:1"));
            assertTrue(output.contains("ERROR"));
            assertTrue(output.contains("paragraph.line-count"));
            assertTrue(output.contains("Paragraph has too many lines"));
            // Should not contain context
            assertFalse(output.contains("Context:"));
        }
    }
    
    @Nested
    @DisplayName("Compact Format Tests")
    class CompactFormatTests {
        
        @Test
        @DisplayName("should render compact single-line format")
        void shouldRenderCompactSingleLineFormat() {
            // Given
            OutputConfiguration config = OutputConfiguration.compactConfig();
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("paragraph.line-count")
                .message("Paragraph has too many lines")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(10)
                    .column(1)
                    .build())
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString().trim();
            
            // Then
            String[] lines = output.split("\n");
            assertEquals(1, lines.length, "Compact format should be single line");
            assertTrue(lines[0].contains("test.adoc:10:1"));
            assertTrue(lines[0].contains("ERROR"));
            assertTrue(lines[0].contains("paragraph.line-count"));
            assertTrue(lines[0].contains("Paragraph has too many lines"));
        }
    }
    
    @Nested
    @DisplayName("Error Grouping Tests")
    class ErrorGroupingTests {
        
        @Test
        @DisplayName("should group errors by rule when enabled")
        void shouldGroupErrorsByRuleWhenEnabled() {
            // Given
            OutputConfiguration config = OutputConfiguration.builder()
                .format(OutputFormat.ENHANCED)
                .errorGrouping(ErrorGroupingConfig.builder()
                    .enabled(true)
                    .groupByRule(true)
                    .maxGroupSize(10)
                    .showSampleErrors(2)
                    .build())
                .build();
            
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationResult.Builder resultBuilder = ValidationResult.builder();
            
            // Add multiple errors with same rule
            for (int i = 1; i <= 5; i++) {
                resultBuilder.addMessage(ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("paragraph.line-count")
                    .message("Paragraph has too many lines")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .line(i * 10)
                        .column(1)
                        .build())
                    .build());
            }
            
            ValidationResult result = resultBuilder.complete().build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("paragraph.line-count (5 occurrences)"));
            assertTrue(output.contains("Showing first 2 errors:"));
            // Should show first 2 errors
            assertTrue(output.contains("test.adoc:10:1"));
            assertTrue(output.contains("test.adoc:20:1"));
            // Should not show all 5
            assertFalse(output.contains("test.adoc:50:1"));
        }
    }
    
    @Nested
    @DisplayName("Summary Tests")
    class SummaryTests {
        
        @Test
        @DisplayName("should render enhanced summary with statistics")
        void shouldRenderEnhancedSummaryWithStatistics() {
            // Given
            OutputConfiguration config = OutputConfiguration.builder()
                .format(OutputFormat.ENHANCED)
                .summary(SummaryConfig.builder()
                    .enabled(true)
                    .showStatistics(true)
                    .showMostCommon(true)
                    .showFileList(true)
                    .showAutoFixHint(true)
                    .build())
                .build();
            
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(createMessage(Severity.ERROR, "rule1", "file1.adoc", true))
                .addMessage(createMessage(Severity.ERROR, "rule1", "file1.adoc", false))
                .addMessage(createMessage(Severity.WARN, "rule2", "file2.adoc", false))
                .addMessage(createMessage(Severity.INFO, "rule3", "file2.adoc", false))
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("Validation Summary"));
            assertTrue(output.contains("Total files scanned:"));
            assertTrue(output.contains("Files with errors:"));
            assertTrue(output.contains("Errors"));
            assertTrue(output.contains("Warnings"));
            assertTrue(output.contains("Info"));
            assertTrue(output.contains("Most common issues:"));
            assertTrue(output.contains("1 error is auto-fixable"));
            assertTrue(output.contains("Files with issues:"));
            assertTrue(output.contains("Summary: 2 errors, 1 warning, 1 info message"));
        }
        
        private ValidationMessage createMessage(Severity severity, String ruleId, String filename, boolean autoFixable) {
            ValidationMessage.Builder builder = ValidationMessage.builder()
                .severity(severity)
                .ruleId(ruleId)
                .message("Test message")
                .location(SourceLocation.builder()
                    .filename(filename)
                    .line(10)
                    .column(1)
                    .build());
            
            if (autoFixable) {
                builder.suggestions(Arrays.asList(
                    Suggestion.builder()
                        .description("Fix it")
                        .isAutoFixable(true)
                        .build()
                ));
            }
            
            return builder.build();
        }
    }
    
    @Nested
    @DisplayName("Empty Result Tests")
    class EmptyResultTests {
        
        @Test
        @DisplayName("should handle empty validation result")
        void shouldHandleEmptyValidationResult() {
            // Given
            OutputConfiguration config = OutputConfiguration.defaultConfig();
            ConsoleFormatter formatter = new ConsoleFormatter(config);
            
            ValidationResult result = ValidationResult.builder()
                .complete()
                .build();
            
            // When
            formatter.format(result, writer);
            String output = this.output.toString();
            
            // Then
            assertTrue(output.contains("No validation issues found"));
        }
    }
}