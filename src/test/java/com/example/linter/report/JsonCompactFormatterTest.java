package com.example.linter.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link JsonCompactFormatter}.
 * 
 * <p>This test class validates the behavior of the JSON compact formatter,
 * which produces single-line JSON output suitable for log processing and
 * pipeline integration.</p>
 * 
 * @see JsonCompactFormatter
 * @see JsonFormatter
 */
@DisplayName("JsonCompactFormatter")
class JsonCompactFormatterTest {
    
    private JsonCompactFormatter formatter;
    private StringWriter stringWriter;
    private PrintWriter printWriter;
    
    @BeforeEach
    void setUp() {
        formatter = new JsonCompactFormatter();
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
    }
    
    @Test
    @DisplayName("should have correct name")
    void shouldHaveCorrectName() {
        // Given/When
        String name = formatter.getName();
        
        // Then
        assertEquals("json-compact", name);
    }
    
    @Test
    @DisplayName("should format empty result as single-line JSON")
    void shouldFormatEmptyResultAsSingleLineJson() {
        // Given
        ValidationResult result = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 100)
            .complete()
            .build();
        
        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();
        
        // Then
        assertNotNull(output);
        assertFalse(output.contains("\n"), "Output should not contain newlines");
        assertFalse(output.contains("  "), "Output should not contain indentation");
        
        // Parse and verify structure
        JsonObject json = JsonParser.parseString(output).getAsJsonObject();
        assertTrue(json.has("timestamp"));
        assertTrue(json.has("duration"));
        assertTrue(json.has("summary"));
        assertTrue(json.has("messages"));
        
        JsonObject summary = json.getAsJsonObject("summary");
        assertEquals(0, summary.get("totalMessages").getAsInt());
        assertEquals(0, summary.get("errors").getAsInt());
        assertEquals(0, summary.get("warnings").getAsInt());
        assertEquals(0, summary.get("infos").getAsInt());
        
        JsonArray messages = json.getAsJsonArray("messages");
        assertEquals(0, messages.size());
    }
    
    @Test
    @DisplayName("should format result with messages as single-line JSON")
    void shouldFormatResultWithMessagesAsSingleLineJson() {
        // Given
        ValidationResult result = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 250)
            .addMessages(Arrays.asList(
                ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("metadata.required")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .startLine(1)
                        .startColumn(5)
                        .build())
                    .message("Missing required attribute: title")
                    .actualValue("null")
                    .expectedValue("non-empty string")
                    .build(),
                ValidationMessage.builder()
                    .severity(Severity.WARN)
                    .ruleId("section.order")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .startLine(10)
                        .build())
                    .message("Section order violation")
                    .build(),
                ValidationMessage.builder()
                    .severity(Severity.INFO)
                    .ruleId("general.info")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .startLine(20)
                        .build())
                    .message("Consider adding description")
                    .build()
            ))
            .complete()
            .build();
        
        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();
        
        // Then
        assertNotNull(output);
        assertFalse(output.contains("\n"), "Output should not contain newlines");
        assertFalse(output.contains("  "), "Output should not contain indentation");
        
        // Parse and verify structure
        JsonObject json = JsonParser.parseString(output).getAsJsonObject();
        
        // Verify summary
        JsonObject summary = json.getAsJsonObject("summary");
        assertEquals(3, summary.get("totalMessages").getAsInt());
        assertEquals(1, summary.get("errors").getAsInt());
        assertEquals(1, summary.get("warnings").getAsInt());
        assertEquals(1, summary.get("infos").getAsInt());
        
        // Verify messages
        JsonArray messages = json.getAsJsonArray("messages");
        assertEquals(3, messages.size());
        
        // Verify first message (error)
        JsonObject error = messages.get(0).getAsJsonObject();
        assertEquals("test.adoc", error.get("file").getAsString());
        assertEquals(1, error.get("line").getAsInt());
        assertEquals(5, error.get("column").getAsInt());
        assertEquals("ERROR", error.get("severity").getAsString());
        assertEquals("Missing required attribute: title", error.get("message").getAsString());
        assertEquals("metadata.required", error.get("ruleId").getAsString());
        assertEquals("null", error.get("actualValue").getAsString());
        assertEquals("non-empty string", error.get("expectedValue").getAsString());
        
        // Verify second message (warning) - no column
        JsonObject warning = messages.get(1).getAsJsonObject();
        assertEquals("test.adoc", warning.get("file").getAsString());
        assertEquals(10, warning.get("line").getAsInt());
        assertFalse(warning.has("column"));
        assertEquals("WARN", warning.get("severity").getAsString());
        assertEquals("section.order", warning.get("ruleId").getAsString());
        
        // Verify third message (info)
        JsonObject info = messages.get(2).getAsJsonObject();
        assertEquals(20, info.get("line").getAsInt());
        assertEquals("INFO", info.get("severity").getAsString());
        assertEquals("general.info", info.get("ruleId").getAsString());
    }
    
    @Test
    @DisplayName("should format duration correctly")
    void shouldFormatDurationCorrectly() {
        // Given - test with milliseconds
        ValidationResult result1 = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 999)
            .complete()
            .build();
        
        // When
        formatter.format(result1, printWriter);
        String output1 = stringWriter.toString();
        
        // Then
        JsonObject json1 = JsonParser.parseString(output1).getAsJsonObject();
        assertEquals("999ms", json1.get("duration").getAsString());
        
        // Given - test with seconds
        stringWriter = new StringWriter();
        printWriter = new PrintWriter(stringWriter);
        ValidationResult result2 = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 1500)
            .complete()
            .build();
        
        // When
        formatter.format(result2, printWriter);
        String output2 = stringWriter.toString();
        
        // Then
        JsonObject json2 = JsonParser.parseString(output2).getAsJsonObject();
        assertEquals("1.500s", json2.get("duration").getAsString());
    }
    
    @Test
    @DisplayName("should handle messages without optional fields")
    void shouldHandleMessagesWithoutOptionalFields() {
        // Given
        ValidationResult result = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 50)
            .addMessages(Arrays.asList(
                ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("basic.error")
                    .location(SourceLocation.builder()
                        .filename("test.adoc")
                        .startLine(1)
                        .build())
                    .message("Basic error")
                    .build()
            ))
            .complete()
            .build();
        
        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();
        
        // Then
        JsonObject json = JsonParser.parseString(output).getAsJsonObject();
        JsonArray messages = json.getAsJsonArray("messages");
        JsonObject message = messages.get(0).getAsJsonObject();
        
        // Required fields
        assertTrue(message.has("file"));
        assertTrue(message.has("line"));
        assertTrue(message.has("severity"));
        assertTrue(message.has("message"));
        
        // Optional fields should not be present
        assertFalse(message.has("column"));
        assertTrue(message.has("ruleId")); // ruleId is always required in our implementation
        assertFalse(message.has("actualValue"));
        assertFalse(message.has("expectedValue"));
    }
    
    @Test
    @DisplayName("should escape special characters properly")
    void shouldEscapeSpecialCharactersProperly() {
        // Given
        ValidationResult result = ValidationResult.builder()
            .startTime(System.currentTimeMillis() - 100)
            .addMessages(Arrays.asList(
                ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("test.escape")
                    .location(SourceLocation.builder()
                        .filename("test/with\"quotes\".adoc")
                        .startLine(1)
                        .build())
                    .message("Message with \"quotes\" and backslash\\")
                    .build()
            ))
            .complete()
            .build();
        
        // When
        formatter.format(result, printWriter);
        String output = stringWriter.toString();
        
        // Then
        // Verify it's valid JSON
        JsonObject json = JsonParser.parseString(output).getAsJsonObject();
        JsonArray messages = json.getAsJsonArray("messages");
        JsonObject message = messages.get(0).getAsJsonObject();
        
        // Gson should handle escaping properly
        assertEquals("test/with\"quotes\".adoc", message.get("file").getAsString());
        assertEquals("Message with \"quotes\" and backslash\\", message.get("message").getAsString());
        
        // Raw output should contain escaped characters
        assertTrue(output.contains("\\\"quotes\\\""));
        assertTrue(output.contains("backslash\\\\"));
    }
}