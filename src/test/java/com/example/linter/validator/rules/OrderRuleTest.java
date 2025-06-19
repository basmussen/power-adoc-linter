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

@DisplayName("OrderRule")
class OrderRuleTest {

    @Nested
    @DisplayName("Rule Building")
    class RuleBuilding {
        
        @Test
        @DisplayName("should build rule with order constraints")
        void shouldBuildRuleWithOrderConstraints() {
            // Given
            OrderRule.Builder builder = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.WARN);
            
            // When
            OrderRule rule = builder.build();
            
            // Then
            assertEquals("metadata.order", rule.getRuleId());
            assertTrue(rule.isApplicable("title"));
            assertTrue(rule.isApplicable("author"));
            assertTrue(rule.isApplicable("revdate"));
            assertFalse(rule.isApplicable("unknown"));
        }
        
        @Test
        @DisplayName("should allow null order for flexible positioning")
        void shouldAllowNullOrderForFlexiblePositioning() {
            // Given
            OrderRule.Builder builder = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("optional", null, Severity.INFO);
            
            // When
            OrderRule rule = builder.build();
            
            // Then
            assertTrue(rule.isApplicable("optional"));
        }
    }

    @Nested
    @DisplayName("Order Validation")
    class OrderValidation {
        
        @Test
        @DisplayName("should pass when attributes are in correct order")
        void shouldPassWhenAttributesAreInCorrectOrder() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 3));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should fail when attributes are out of order")
        void shouldFailWhenAttributesAreOutOfOrder() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("revdate", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 3));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 4));
            
            List<ValidationMessage> messages = rule.validateOrder();
            
            // Then
            assertFalse(messages.isEmpty());
            
            boolean foundTitleError = messages.stream()
                .anyMatch(m -> m.getMessage().contains("'title' should appear before 'author'"));
            assertTrue(foundTitleError);
        }
        
        @Test
        @DisplayName("should detect multiple order violations")
        void shouldDetectMultipleOrderViolations() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("version", 3, Severity.ERROR)
                .addOrderConstraint("revdate", 4, Severity.ERROR)
                .build();
            
            // When
            rule.validate("version", "1.0", createLocation("test.adoc", 1));
            rule.validate("revdate", "2024-01-15", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 3));
            rule.validate("author", "John Doe", createLocation("test.adoc", 4));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.size() >= 2);
        }
    }

    @Nested
    @DisplayName("Partial Order Validation")
    class PartialOrderValidation {
        
        @Test
        @DisplayName("should handle missing attributes in order check")
        void shouldHandleMissingAttributesInOrderCheck() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .addOrderConstraint("version", 3, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("version", "1.0", createLocation("test.adoc", 2));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should ignore attributes without order constraint")
        void shouldIgnoreAttributesWithoutOrderConstraint() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .build();
            
            // When
            rule.validate("title", "My Document", createLocation("test.adoc", 1));
            rule.validate("keywords", "test, doc", createLocation("test.adoc", 2));
            rule.validate("author", "John Doe", createLocation("test.adoc", 3));
            
            // Then
            List<ValidationMessage> messages = rule.validateOrder();
            assertTrue(messages.isEmpty());
        }
    }

    @Nested
    @DisplayName("Message Details")
    class MessageDetails {
        
        @Test
        @DisplayName("should include line numbers in error messages")
        void shouldIncludeLineNumbersInErrorMessages() {
            // Given
            OrderRule rule = OrderRule.builder()
                .addOrderConstraint("title", 1, Severity.ERROR)
                .addOrderConstraint("author", 2, Severity.ERROR)
                .build();
            
            // When
            rule.validate("author", "John Doe", createLocation("test.adoc", 2));
            rule.validate("title", "My Document", createLocation("test.adoc", 5));
            
            List<ValidationMessage> messages = rule.validateOrder();
            
            // Then
            assertEquals(1, messages.size());
            
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertTrue(message.getActualValue().get().contains("Line 5"));
            assertTrue(message.getExpectedValue().get().contains("Before line 2"));
        }
    }
    
    private SourceLocation createLocation(String filename, int line) {
        return SourceLocation.builder()
            .filename(filename)
            .line(line)
            .build();
    }
}