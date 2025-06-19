package com.example.linter.validator;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationMessage")
class ValidationMessageTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder()
            .filename("test.adoc")
            .line(10)
            .build();
    }

    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        
        @Test
        @DisplayName("should create message with required fields")
        void shouldCreateMessageWithRequiredFields() {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("metadata.required")
                .message("Missing required attribute")
                .location(testLocation)
                .build();
            
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.required", message.getRuleId());
            assertEquals("Missing required attribute", message.getMessage());
            assertEquals(testLocation, message.getLocation());
        }
        
        @Test
        @DisplayName("should create message with all fields")
        void shouldCreateMessageWithAllFields() {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("metadata.pattern")
                .message("Invalid format")
                .location(testLocation)
                .attributeName("author")
                .actualValue("john")
                .expectedValue("Pattern '^[A-Z].*'")
                .build();
            
            assertTrue(message.getAttributeName().isPresent());
            assertEquals("author", message.getAttributeName().get());
            assertTrue(message.getActualValue().isPresent());
            assertEquals("john", message.getActualValue().get());
            assertTrue(message.getExpectedValue().isPresent());
            assertEquals("Pattern '^[A-Z].*'", message.getExpectedValue().get());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            assertThrows(NullPointerException.class, () ->
                ValidationMessage.builder()
                    .ruleId("test")
                    .message("test")
                    .location(testLocation)
                    .build()
            );
        }
        
        @Test
        @DisplayName("should require location")
        void shouldRequireLocation() {
            assertThrows(NullPointerException.class, () ->
                ValidationMessage.builder()
                    .severity(Severity.ERROR)
                    .ruleId("test")
                    .message("test")
                    .build()
            );
        }
    }

    @Nested
    @DisplayName("Formatting")
    class FormattingTest {
        
        @Test
        @DisplayName("should format simple message")
        void shouldFormatSimpleMessage() {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("metadata.required")
                .message("Missing required attribute 'author'")
                .location(testLocation)
                .build();
            
            String formatted = message.format();
            assertTrue(formatted.contains("test.adoc:10"));
            assertTrue(formatted.contains("[ERROR]"));
            assertTrue(formatted.contains("Missing required attribute 'author'"));
        }
        
        @Test
        @DisplayName("should format message with actual and expected values")
        void shouldFormatMessageWithValues() {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("metadata.pattern")
                .message("Invalid format")
                .location(testLocation)
                .actualValue("john")
                .expectedValue("Pattern '^[A-Z].*'")
                .build();
            
            String formatted = message.format();
            assertTrue(formatted.contains("Found: \"john\""));
            assertTrue(formatted.contains("Expected: Pattern '^[A-Z].*'"));
        }
        
        @Test
        @DisplayName("should format message with only actual value")
        void shouldFormatMessageWithOnlyActualValue() {
            ValidationMessage message = ValidationMessage.builder()
                .severity(Severity.INFO)
                .ruleId("metadata.length")
                .message("Value detected")
                .location(testLocation)
                .actualValue("Some long text...")
                .build();
            
            String formatted = message.format();
            assertTrue(formatted.contains("Found: \"Some long text...\""));
            assertFalse(formatted.contains("Expected:"));
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTest {
        
        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualForSameValues() {
            ValidationMessage message1 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test.rule")
                .message("Test message")
                .location(testLocation)
                .build();
            
            ValidationMessage message2 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test.rule")
                .message("Test message")
                .location(testLocation)
                .build();
            
            assertEquals(message1, message2);
            assertEquals(message1.hashCode(), message2.hashCode());
        }
        
        @Test
        @DisplayName("should not be equal for different severities")
        void shouldNotBeEqualForDifferentSeverities() {
            ValidationMessage message1 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test.rule")
                .message("Test message")
                .location(testLocation)
                .build();
            
            ValidationMessage message2 = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("test.rule")
                .message("Test message")
                .location(testLocation)
                .build();
            
            assertNotEquals(message1, message2);
        }
    }
}