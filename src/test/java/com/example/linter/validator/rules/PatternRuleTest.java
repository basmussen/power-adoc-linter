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

@DisplayName("PatternRule")
class PatternRuleTest {

    private SourceLocation testLocation;

    @BeforeEach
    void setUp() {
        testLocation = SourceLocation.builder()
            .filename("test.adoc")
            .line(2)
            .build();
    }

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with valid patterns")
        void shouldBuildRuleWithValidPatterns() {
            PatternRule rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .addPattern("version", "^\\d+\\.\\d+(\\.\\d+)?$", Severity.ERROR)
                .addPattern("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", Severity.WARN)
                .build();
            
            assertEquals("metadata.pattern", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("version"));
            assertTrue(rule.isApplicable("email"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should reject invalid regex pattern")
        void shouldRejectInvalidRegexPattern() {
            PatternRule.Builder builder = PatternRule.builder();
            
            assertThrows(IllegalArgumentException.class, () ->
                builder.addPattern("invalid", "[", Severity.ERROR)
            );
        }
    }

    @Nested
    @DisplayName("Title Pattern Validation")
    class TitlePatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should pass when title starts with uppercase")
        void shouldPassWhenTitleStartsWithUppercase() {
            List<ValidationMessage> messages = rule.validate("title", "My Document Title", testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when title starts with lowercase")
        void shouldFailWhenTitleStartsWithLowercase() {
            List<ValidationMessage> messages = rule.validate("title", "my document title", testLocation);
            
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("metadata.pattern", message.getRuleId());
            assertTrue(message.getActualValue().get().contains("my document title"));
            assertTrue(message.getExpectedValue().get().contains("^[A-Z].*"));
        }
    }

    @Nested
    @DisplayName("Version Pattern Validation")
    class VersionPatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("version", "^\\d+\\.\\d+(\\.\\d+)?$", Severity.ERROR)
                .build();
        }
        
        @Test
        @DisplayName("should accept semantic version format")
        void shouldAcceptSemanticVersionFormat() {
            assertTrue(rule.validate("version", "1.0", testLocation).isEmpty());
            assertTrue(rule.validate("version", "1.0.0", testLocation).isEmpty());
            assertTrue(rule.validate("version", "2.15.3", testLocation).isEmpty());
        }
        
        @Test
        @DisplayName("should reject invalid version format")
        void shouldRejectInvalidVersionFormat() {
            assertFalse(rule.validate("version", "1", testLocation).isEmpty());
            assertFalse(rule.validate("version", "1.0.0.0", testLocation).isEmpty());
            assertFalse(rule.validate("version", "v1.0.0", testLocation).isEmpty());
            assertFalse(rule.validate("version", "1.0-SNAPSHOT", testLocation).isEmpty());
        }
    }

    @Nested
    @DisplayName("Email Pattern Validation")
    class EmailPatternValidation {
        
        private PatternRule rule;
        
        @BeforeEach
        void setUp() {
            rule = PatternRule.builder()
                .addPattern("email", "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", Severity.WARN)
                .build();
        }
        
        @Test
        @DisplayName("should accept valid email formats")
        void shouldAcceptValidEmailFormats() {
            assertTrue(rule.validate("email", "user@example.com", testLocation).isEmpty());
            assertTrue(rule.validate("email", "john.doe@company.co.uk", testLocation).isEmpty());
            assertTrue(rule.validate("email", "test+tag@domain.org", testLocation).isEmpty());
        }
        
        @Test
        @DisplayName("should reject invalid email formats")
        void shouldRejectInvalidEmailFormats() {
            assertFalse(rule.validate("email", "invalid", testLocation).isEmpty());
            assertFalse(rule.validate("email", "@example.com", testLocation).isEmpty());
            assertFalse(rule.validate("email", "user@", testLocation).isEmpty());
            assertFalse(rule.validate("email", "user@domain", testLocation).isEmpty());
        }
    }

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should skip validation for null values")
        void shouldSkipValidationForNullValues() {
            PatternRule rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
            
            List<ValidationMessage> messages = rule.validate("title", null, testLocation);
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should skip validation for empty values")
        void shouldSkipValidationForEmptyValues() {
            PatternRule rule = PatternRule.builder()
                .addPattern("title", "^[A-Z].*", Severity.ERROR)
                .build();
            
            List<ValidationMessage> messages = rule.validate("title", "", testLocation);
            assertTrue(messages.isEmpty());
        }
    }
}