package com.example.linter.report;

import com.example.linter.config.Severity;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JsonFormatter")
class JsonFormatterTest {
    
    private JsonFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() {
        formatter = new JsonFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }
    
    @Nested
    @DisplayName("Basic JSON Structure")
    class BasicJsonStructure {
        
        @Test
        @DisplayName("should format empty result as valid JSON")
        void shouldFormatEmptyResultAsValidJson() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"timestamp\":"));
            assertTrue(output.contains("\"duration\":"));
            assertTrue(output.contains("\"summary\": {"));
            assertTrue(output.contains("\"totalMessages\": 0"));
            assertTrue(output.contains("\"errors\": 0"));
            assertTrue(output.contains("\"warnings\": 0"));
            assertTrue(output.contains("\"infos\": 0"));
            assertTrue(output.contains("\"messages\": ["));
            assertTrue(output.trim().startsWith("{"));
            assertTrue(output.trim().endsWith("}"));
        }
        
        @Test
        @DisplayName("should format single message as valid JSON")
        void shouldFormatSingleMessageAsValidJson() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("required-attribute")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .startLine(10)
                    .build())
                .message("Missing required attribute")
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"totalMessages\": 1"));
            assertTrue(output.contains("\"errors\": 1"));
            assertTrue(output.contains("\"file\": \"test.adoc\""));
            assertTrue(output.contains("\"line\": 10"));
            assertTrue(output.contains("\"severity\": \"ERROR\""));
            assertTrue(output.contains("\"message\": \"Missing required attribute\""));
        }
        
        @Test
        @DisplayName("should return correct name")
        void shouldReturnCorrectName() {
            assertEquals("json", formatter.getName());
        }
    }
    
    @Nested
    @DisplayName("JSON Escaping")
    class JsonEscaping {
        
        @Test
        @DisplayName("should escape special characters in messages")
        void shouldEscapeSpecialCharacters() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("test-rule")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .startLine(5)
                    .build())
                .message("Message with \"quotes\" and \nnewline")
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("Message with \\\"quotes\\\" and \\nnewline"));
        }
        
        @Test
        @DisplayName("should escape backslashes")
        void shouldEscapeBackslashes() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.INFO)
                .ruleId("test-rule")
                .location(SourceLocation.builder()
                    .filename("C:\\path\\to\\file.adoc")
                    .startLine(1)
                    .build())
                .message("Path with backslashes")
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("C:\\\\path\\\\to\\\\file.adoc"));
        }
    }
    
    @Nested
    @DisplayName("Optional Fields")
    class OptionalFields {
        
        @Test
        @DisplayName("should include optional fields when present")
        void shouldIncludeOptionalFields() {
            // Given
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("value-check")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .startLine(15)
                    .startColumn(20)
                    .build())
                .message("Invalid value")
                .actualValue("100")
                .expectedValue("80")
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(message)
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"column\": 20"));
            assertTrue(output.contains("\"ruleId\": \"value-check\""));
            assertTrue(output.contains("\"actualValue\": \"100\""));
            assertTrue(output.contains("\"expectedValue\": \"80\""));
        }
    }
    
    @Nested
    @DisplayName("Multiple Messages")
    class MultipleMessages {
        
        @Test
        @DisplayName("should format multiple messages with proper commas")
        void shouldFormatMultipleMessagesWithProperCommas() {
            // Given
            ValidationMessage msg1 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test-rule")
                .location(SourceLocation.builder()
                    .filename("file1.adoc")
                    .startLine(10)
                    .build())
                .message("First error")
                .build();
            
            ValidationMessage msg2 = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("test-rule")
                .location(SourceLocation.builder()
                    .filename("file2.adoc")
                    .startLine(20)
                    .build())
                .message("Second warning")
                .build();
            
            ValidationResult result = ValidationResult.builder()
                .addMessage(msg1)
                .addMessage(msg2)
                .complete()
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"totalMessages\": 2"));
            assertTrue(output.contains("\"errors\": 1"));
            assertTrue(output.contains("\"warnings\": 1"));
            // Check that there's a comma between messages
            int firstMessageEnd = output.indexOf("},", output.indexOf("\"message\": \"First error\""));
            int secondMessageStart = output.indexOf("{", firstMessageEnd);
            assertTrue(firstMessageEnd < secondMessageStart);
        }
    }
    
    @Nested
    @DisplayName("Duration Formatting")
    class DurationFormatting {
        
        @Test
        @DisplayName("should format duration in milliseconds when less than 1 second")
        void shouldFormatDurationInMilliseconds() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .startTime(1000)
                .endTime(1500)
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"duration\": \"500ms\""));
        }
        
        @Test
        @DisplayName("should format duration in seconds when 1 second or more")
        void shouldFormatDurationInSeconds() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .startTime(1000)
                .endTime(3500)
                .build();
            
            // When
            formatter.format(result, printWriter);
            printWriter.flush();
            
            // Then
            String output = stringWriter.toString();
            assertTrue(output.contains("\"duration\": \"2.500s\""));
        }
    }
}