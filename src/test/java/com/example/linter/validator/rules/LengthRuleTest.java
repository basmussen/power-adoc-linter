package com.example.linter.validator.rules;

import com.example.linter.config.Severity;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("LengthRule")
class LengthRuleTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder()
            .filename("test.adoc")
            .line(3)
            .build();
    }

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with min and max constraints")
        void shouldBuildRuleWithMinAndMaxConstraints() {
            // Given
            LengthRule.Builder builder = LengthRule.builder()
                .addLengthConstraint("title", 5, 100, Severity.ERROR)
                .addLengthConstraint("author", 3, 50, Severity.ERROR);
            
            // When
            LengthRule rule = builder.build();
            
            // Then
            assertEquals("metadata.length", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("author"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should build rule with only min constraint")
        void shouldBuildRuleWithOnlyMinConstraint() {
            // Given
            LengthRule.Builder builder = LengthRule.builder()
                .addLengthConstraint("description", 10, null, Severity.WARN);
            
            // When
            LengthRule rule = builder.build();
            
            // Then
            assertTrue(rule.isApplicable("description"));
        }
        
        @Test
        @DisplayName("should build rule with only max constraint")
        void shouldBuildRuleWithOnlyMaxConstraint() {
            // Given
            LengthRule.Builder builder = LengthRule.builder()
                .addLengthConstraint("keywords", null, 200, Severity.INFO);
            
            // When
            LengthRule rule = builder.build();
            
            // Then
            assertTrue(rule.isApplicable("keywords"));
        }
        
        @Test
        @DisplayName("should reject when neither min nor max specified")
        void shouldRejectWhenNeitherMinNorMaxSpecified() {
            // Given
            LengthRule.Builder builder = LengthRule.builder();
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                builder.addLengthConstraint("invalid", null, null, Severity.ERROR)
            );
        }
        
        @Test
        @DisplayName("should reject when min greater than max")
        void shouldRejectWhenMinGreaterThanMax() {
            // Given
            LengthRule.Builder builder = LengthRule.builder();
            
            // When/Then
            assertThrows(IllegalArgumentException.class, () ->
                builder.addLengthConstraint("invalid", 100, 50, Severity.ERROR)
            );
        }
    }

    @Nested
    @DisplayName("Min Length Validation")
    class MinLengthValidation {
        
        private LengthRule rule;
        
        @BeforeEach
        void setUp() {
            rule = LengthRule.builder()
                .addLengthConstraint("title", 5, null, Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should pass when value meets minimum length")
        void shouldPassWhenValueMeetsMinimumLength() {
            // Given
            String validTitle = "Valid Title";
            
            // When
            List<ValidationMessage> messages = rule.validate("title", validTitle, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when value is too short")
        void shouldFailWhenValueIsTooShort() {
            // Given
            String shortTitle = "Hi";
            
            // When
            List<ValidationMessage> messages = rule.validate("title", shortTitle, testLocation);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.length.min", message.getRuleId());
            assertTrue(message.getMessage().contains("too short"));
            assertTrue(message.getActualValue().get().contains("2 characters"));
            assertTrue(message.getExpectedValue().get().contains("Minimum 5"));
        }
    }

    @Nested
    @DisplayName("Max Length Validation")
    class MaxLengthValidation {
        
        private LengthRule rule;
        
        @BeforeEach
        void setUp() {
            rule = LengthRule.builder()
                .addLengthConstraint("author", null, 50, Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should pass when value within maximum length")
        void shouldPassWhenValueWithinMaximumLength() {
            // Given
            String validAuthor = "John Doe";
            
            // When
            List<ValidationMessage> messages = rule.validate("author", validAuthor, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when value is too long")
        void shouldFailWhenValueIsTooLong() {
            // Given
            String longName = "This is a very long author name that exceeds the maximum allowed length";
            
            // When
            List<ValidationMessage> messages = rule.validate("author", longName, testLocation);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.length.max", message.getRuleId());
            assertTrue(message.getMessage().contains("too long"));
            assertTrue(message.getExpectedValue().get().contains("Maximum 50"));
        }
    }

    @Nested
    @DisplayName("Combined Min Max Validation")
    class CombinedMinMaxValidation {
        
        private LengthRule rule;
        
        @BeforeEach
        void setUp() {
            rule = LengthRule.builder()
                .addLengthConstraint("title", 5, 100, Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should pass when value is within range")
        void shouldPassWhenValueIsWithinRange() {
            // Given
            String validTitle = "Perfect Title";
            
            // When
            List<ValidationMessage> messages = rule.validate("title", validTitle, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail with both min and max messages when applicable")
        void shouldFailWithBothMinAndMaxMessagesWhenApplicable() {
            // Given
            String shortTitle = "Hi";
            String veryLongTitle = "A".repeat(150);
            
            // When
            List<ValidationMessage> tooShort = rule.validate("title", shortTitle, testLocation);
            List<ValidationMessage> tooLong = rule.validate("title", veryLongTitle, testLocation);
            
            // Then
            assertEquals(1, tooShort.size());
            assertEquals("metadata.length.min", tooShort.get(0).getRuleId());
            
            assertEquals(1, tooLong.size());
            assertEquals("metadata.length.max", tooLong.get(0).getRuleId());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should skip validation for null values")
        void shouldSkipValidationForNullValues() {
            // Given
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("title", 5, 100, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("title", null, testLocation);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should validate empty string as zero length")
        void shouldValidateEmptyStringAsZeroLength() {
            // Given
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("title", 1, 100, Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = rule.validate("title", "", testLocation);
            
            // Then
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getActualValue().get().contains("0 characters"));
        }
    }
}