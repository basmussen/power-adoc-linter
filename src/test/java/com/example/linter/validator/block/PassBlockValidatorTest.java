package com.example.linter.validator.block;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.PassBlock;
import com.example.linter.config.blocks.PassBlock.ContentConfig;
import com.example.linter.config.blocks.PassBlock.JustificationConfig;
import com.example.linter.config.blocks.PassBlock.TypeConfig;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PassBlockValidatorTest {
    
    private PassBlockValidator validator;
    
    @Mock
    private StructuralNode mockBlock;
    
    @Mock
    private BlockValidationContext mockContext;
    
    @Mock
    private SourceLocation mockLocation;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validator = new PassBlockValidator();
        
        // Default setup
        when(mockContext.createLocation(any())).thenReturn(mockLocation);
        when(mockBlock.getContent()).thenReturn("<div>Test content</div>");
    }
    
    @Test
    @DisplayName("should return PASS as supported type")
    void shouldReturnPassAsSupportedType() {
        assertEquals(BlockType.PASS, validator.getSupportedType());
    }
    
    @Nested
    @DisplayName("Type Validation")
    class TypeValidationTests {
        
        @Test
        @DisplayName("should validate required type when missing")
        void shouldValidateRequiredTypeWhenMissing() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn(null);
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .type(TypeConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.type.required", message.getRuleId());
            assertTrue(message.getMessage().contains("must specify a type"));
        }
        
        @Test
        @DisplayName("should validate allowed types")
        void shouldValidateAllowedTypes() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn("javascript");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .type(TypeConfig.builder()
                    .required(true)
                    .allowed(Arrays.asList("html", "xml", "svg"))
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.type.allowed", message.getRuleId());
            assertEquals("javascript", message.getActualValue().orElse(null));
            assertTrue(message.getExpectedValue().orElse("").contains("html, xml, svg"));
        }
        
        @Test
        @DisplayName("should pass when type is valid")
        void shouldPassWhenTypeIsValid() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn("html");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .type(TypeConfig.builder()
                    .required(true)
                    .allowed(Arrays.asList("html", "xml", "svg"))
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should use block severity when type severity is null")
        void shouldUseBlockSeverityWhenTypeSeverityIsNull() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn(null);
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.WARN)
                .type(TypeConfig.builder()
                    .required(true)
                    .severity(null) // No severity specified
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
    }
    
    @Nested
    @DisplayName("Content Validation")
    class ContentValidationTests {
        
        @Test
        @DisplayName("should validate required content when missing")
        void shouldValidateRequiredContentWhenMissing() {
            // Given
            when(mockBlock.getContent()).thenReturn("");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .content(ContentConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.content.required", message.getRuleId());
        }
        
        @Test
        @DisplayName("should validate max length")
        void shouldValidateMaxLength() {
            // Given
            String longContent = "x".repeat(100);
            when(mockBlock.getContent()).thenReturn(longContent);
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .content(ContentConfig.builder()
                    .maxLength(50)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.content.maxLength", message.getRuleId());
            assertEquals("100 characters", message.getActualValue().orElse(null));
            assertEquals("Maximum 50 characters", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate content pattern")
        void shouldValidateContentPattern() {
            // Given
            when(mockBlock.getContent()).thenReturn("<script>alert('bad')</script>");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .content(ContentConfig.builder()
                    .pattern("^<[^>]+>.*</[^>]+>$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertTrue(messages.isEmpty()); // Pattern matches
        }
        
        @Test
        @DisplayName("should fail when content does not match pattern")
        void shouldFailWhenContentDoesNotMatchPattern() {
            // Given
            when(mockBlock.getContent()).thenReturn("plain text");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .content(ContentConfig.builder()
                    .pattern("^<[^>]+>.*</[^>]+>$")
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("pass.content.pattern", messages.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("Justification Validation")
    class JustificationValidationTests {
        
        @Test
        @DisplayName("should validate required justification when missing")
        void shouldValidateRequiredJustificationWhenMissing() {
            // Given
            when(mockBlock.getAttribute("pass-reason")).thenReturn(null);
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .justification(JustificationConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.ERROR, message.getSeverity());
            assertEquals("pass.justification.required", message.getRuleId());
            assertTrue(message.getMessage().contains("must provide justification"));
        }
        
        @Test
        @DisplayName("should validate min length of justification")
        void shouldValidateMinLengthOfJustification() {
            // Given
            when(mockBlock.getAttribute("pass-reason")).thenReturn("Too short");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .justification(JustificationConfig.builder()
                    .required(true)
                    .minLength(20)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.WARN, message.getSeverity());
            assertEquals("pass.justification.minLength", message.getRuleId());
            assertEquals("9 characters", message.getActualValue().orElse(null));
            assertEquals("At least 20 characters", message.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate max length of justification")
        void shouldValidateMaxLengthOfJustification() {
            // Given
            String longReason = "This is a very long justification that exceeds the maximum allowed length";
            when(mockBlock.getAttribute("pass-reason")).thenReturn(longReason);
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .justification(JustificationConfig.builder()
                    .required(true)
                    .maxLength(50)
                    .severity(Severity.INFO)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage message = messages.get(0);
            assertEquals(Severity.INFO, message.getSeverity());
            assertEquals("pass.justification.maxLength", message.getRuleId());
        }
        
        @Test
        @DisplayName("should pass when justification is valid")
        void shouldPassWhenJustificationIsValid() {
            // Given
            when(mockBlock.getAttribute("pass-reason")).thenReturn("Custom widget for product gallery display");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .justification(JustificationConfig.builder()
                    .required(true)
                    .minLength(20)
                    .maxLength(200)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {
        
        @Test
        @DisplayName("should validate all rules together")
        void shouldValidateAllRulesTogether() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn("html");
            when(mockBlock.getAttribute("pass-reason")).thenReturn("Custom widget for product gallery display");
            when(mockBlock.getContent()).thenReturn("<div class=\"product-slider\">Content</div>");
            
            PassBlock config = PassBlock.builder()
                .name("Passthrough Block")
                .severity(Severity.ERROR)
                .type(TypeConfig.builder()
                    .required(true)
                    .allowed(Arrays.asList("html", "xml", "svg"))
                    .severity(Severity.ERROR)
                    .build())
                .content(ContentConfig.builder()
                    .required(true)
                    .maxLength(1000)
                    .pattern("^<[^>]+>.*</[^>]+>$")
                    .severity(Severity.ERROR)
                    .build())
                .justification(JustificationConfig.builder()
                    .required(true)
                    .minLength(20)
                    .maxLength(200)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should collect multiple validation errors")
        void shouldCollectMultipleValidationErrors() {
            // Given
            when(mockBlock.getAttribute("pass-type")).thenReturn(null);
            when(mockBlock.getAttribute("pass-reason")).thenReturn("Short");
            when(mockBlock.getContent()).thenReturn("");
            
            PassBlock config = PassBlock.builder()
                .severity(Severity.ERROR)
                .type(TypeConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .content(ContentConfig.builder()
                    .required(true)
                    .severity(Severity.WARN)
                    .build())
                .justification(JustificationConfig.builder()
                    .required(true)
                    .minLength(20)
                    .severity(Severity.INFO)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, mockContext);
            
            // Then
            assertEquals(3, messages.size());
            
            // Verify different severities
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.ERROR));
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.WARN));
            assertTrue(messages.stream().anyMatch(m -> m.getSeverity() == Severity.INFO));
        }
    }
}