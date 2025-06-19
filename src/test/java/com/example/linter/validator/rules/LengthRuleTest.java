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
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("title", 5, 100, Severity.ERROR)
                .addLengthConstraint("author", 3, 50, Severity.ERROR)
                .build();
            
            assertEquals("metadata.length", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("author"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should build rule with only min constraint")
        void shouldBuildRuleWithOnlyMinConstraint() {
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("description", 10, null, Severity.WARN)
                .build();
            
            assertTrue(rule.isApplicable("description"));
        }
        
        @Test
        @DisplayName("should build rule with only max constraint")
        void shouldBuildRuleWithOnlyMaxConstraint() {
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("keywords", null, 200, Severity.INFO)
                .build();
            
            assertTrue(rule.isApplicable("keywords"));
        }
        
        @Test
        @DisplayName("should reject when neither min nor max specified")
        void shouldRejectWhenNeitherMinNorMaxSpecified() {
            LengthRule.Builder builder = LengthRule.builder();
            
            assertThrows(IllegalArgumentException.class, () ->
                builder.addLengthConstraint("invalid", null, null, Severity.ERROR)
            );
        }
        
        @Test
        @DisplayName("should reject when min greater than max")
        void shouldRejectWhenMinGreaterThanMax() {
            LengthRule.Builder builder = LengthRule.builder();
            
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
            List<ValidationMessage> messages = rule.validate("title", "Valid Title", testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when value is too short")
        void shouldFailWhenValueIsTooShort() {
            List<ValidationMessage> messages = rule.validate("title", "Hi", testLocation);
            
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
            List<ValidationMessage> messages = rule.validate("author", "John Doe", testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when value is too long")
        void shouldFailWhenValueIsTooLong() {
            String longName = "This is a very long author name that exceeds the maximum allowed length";
            List<ValidationMessage> messages = rule.validate("author", longName, testLocation);
            
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
            List<ValidationMessage> messages = rule.validate("title", "Perfect Title", testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail with both min and max messages when applicable")
        void shouldFailWithBothMinAndMaxMessagesWhenApplicable() {
            List<ValidationMessage> tooShort = rule.validate("title", "Hi", testLocation);
            assertEquals(1, tooShort.size());
            assertEquals("metadata.length.min", tooShort.get(0).getRuleId());
            
            String veryLongTitle = "A".repeat(150);
            List<ValidationMessage> tooLong = rule.validate("title", veryLongTitle, testLocation);
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
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("title", 5, 100, Severity.ERROR)
                .build();
            
            List<ValidationMessage> messages = rule.validate("title", null, testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should validate empty string as zero length")
        void shouldValidateEmptyStringAsZeroLength() {
            LengthRule rule = LengthRule.builder()
                .addLengthConstraint("title", 1, 100, Severity.ERROR)
                .build();
            
            List<ValidationMessage> messages = rule.validate("title", "", testLocation);
            assertEquals(1, messages.size());
            assertTrue(messages.get(0).getActualValue().get().contains("0 characters"));
        }
    }
}