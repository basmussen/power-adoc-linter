package com.example.linter.validator.block;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.AdmonitionBlock;
import com.example.linter.config.rule.LineConfig;
import com.example.linter.validator.ValidationMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("AdmonitionBlockValidator Tests")
class AdmonitionBlockValidatorTest {
    
    private AdmonitionBlockValidator validator;
    private BlockValidationContext context;
    private StructuralNode mockBlock;
    private Document mockDocument;
    
    @BeforeEach
    void setUp() {
        validator = new AdmonitionBlockValidator();
        context = mock(BlockValidationContext.class);
        mockBlock = mock(StructuralNode.class);
        mockDocument = mock(Document.class);
        
        when(mockBlock.getDocument()).thenReturn(mockDocument);
        when(context.createLocation(any())).thenReturn(mock(com.example.linter.validator.SourceLocation.class));
    }
    
    @Test
    @DisplayName("should support ADMONITION block type")
    void shouldSupportAdmonitionBlockType() {
        assertEquals(BlockType.ADMONITION, validator.getSupportedType());
    }
    
    @Nested
    @DisplayName("Title Validation")
    class TitleValidation {
        
        @Test
        @DisplayName("should validate required title")
        void shouldValidateRequiredTitle() {
            // Given
            when(mockBlock.getTitle()).thenReturn(null);
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .title(AdmonitionBlock.TitleConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.title.required", messages.get(0).getRuleId());
        }
        
        @Test
        @DisplayName("should validate title pattern")
        void shouldValidateTitlePattern() {
            // Given
            when(mockBlock.getTitle()).thenReturn("invalid title");
            when(mockBlock.getStyle()).thenReturn("WARNING");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .title(AdmonitionBlock.TitleConfig.builder()
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.title.pattern", messages.get(0).getRuleId());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
        
        @Test
        @DisplayName("should validate title length constraints")
        void shouldValidateTitleLength() {
            // Given
            when(mockBlock.getTitle()).thenReturn("Hi");
            when(mockBlock.getStyle()).thenReturn("TIP");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .title(AdmonitionBlock.TitleConfig.builder()
                    .minLength(3)
                    .maxLength(50)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.title.minLength", messages.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("Content Validation")
    class ContentValidation {
        
        @Test
        @DisplayName("should validate content min length")
        void shouldValidateContentMinLength() {
            // Given
            when(mockBlock.getContent()).thenReturn("Short");
            when(mockBlock.getStyle()).thenReturn("IMPORTANT");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .content(AdmonitionBlock.ContentConfig.builder()
                    .minLength(10)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.content.minLength", messages.get(0).getRuleId());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
        
        @Test
        @DisplayName("should validate content max length")
        void shouldValidateContentMaxLength() {
            // Given
            String longContent = "This is a very long content that exceeds the maximum allowed length";
            when(mockBlock.getContent()).thenReturn(longContent);
            when(mockBlock.getStyle()).thenReturn("CAUTION");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .content(AdmonitionBlock.ContentConfig.builder()
                    .maxLength(50)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.content.maxLength", messages.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("Line Count Validation")
    class LineCountValidation {
        
        @Test
        @DisplayName("should validate minimum lines")
        void shouldValidateMinLines() {
            // Given
            when(mockBlock.getContent()).thenReturn("Single line");
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .content(AdmonitionBlock.ContentConfig.builder()
                    .lines(LineConfig.builder()
                        .min(2)
                        .severity(Severity.INFO)
                        .build())
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.content.lines.min", messages.get(0).getRuleId());
            assertEquals(Severity.INFO, messages.get(0).getSeverity());
        }
        
        @Test
        @DisplayName("should validate maximum lines")
        void shouldValidateMaxLines() {
            // Given
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3\nLine 4");
            when(mockBlock.getStyle()).thenReturn("TIP");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .content(AdmonitionBlock.ContentConfig.builder()
                    .lines(LineConfig.builder()
                        .max(3)
                        .build())
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.content.lines.max", messages.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("Icon Validation")
    class IconValidation {
        
        @Test
        @DisplayName("should validate icon is required")
        void shouldValidateIconRequired() {
            // Given
            when(mockDocument.getAttribute("icons")).thenReturn(null);
            when(mockBlock.getAttribute("icon")).thenReturn(null);
            when(mockBlock.getStyle()).thenReturn("WARNING");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .icon(AdmonitionBlock.IconConfig.builder()
                    .required(true)
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.icon.required", messages.get(0).getRuleId());
        }
        
        @Test
        @DisplayName("should validate icon pattern")
        void shouldValidateIconPattern() {
            // Given
            when(mockDocument.getAttribute("icons")).thenReturn("font");
            when(mockBlock.getAttribute("icon")).thenReturn("invalid-icon");
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .icon(AdmonitionBlock.IconConfig.builder()
                    .pattern("^(info|warning|caution|tip|note)$")
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.icon.pattern", messages.get(0).getRuleId());
        }
        
        @Test
        @DisplayName("should detect icon from document level")
        void shouldDetectIconFromDocument() {
            // Given
            when(mockDocument.getAttribute("icons")).thenReturn("font");
            when(mockBlock.getStyle()).thenReturn("TIP");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .icon(AdmonitionBlock.IconConfig.builder()
                    .required(true)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Type Validation")
    class TypeValidation {
        
        @Test
        @DisplayName("should validate type is required")
        void shouldValidateTypeRequired() {
            // Given
            when(mockBlock.getStyle()).thenReturn(null);
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .type(AdmonitionBlock.TypeConfig.builder()
                    .required(true)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.type.required", messages.get(0).getRuleId());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
            assertTrue(messages.get(0).getActualValue().isPresent());
            assertEquals("No type", messages.get(0).getActualValue().get());
        }
        
        @Test
        @DisplayName("should validate allowed types")
        void shouldValidateAllowedTypes() {
            // Given
            when(mockBlock.getStyle()).thenReturn("CUSTOM");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .type(AdmonitionBlock.TypeConfig.builder()
                    .allowed(List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION"))
                    .severity(Severity.ERROR)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.type.allowed", messages.get(0).getRuleId());
            assertEquals(Severity.ERROR, messages.get(0).getSeverity());
            assertTrue(messages.get(0).getActualValue().isPresent());
            assertEquals("CUSTOM", messages.get(0).getActualValue().get());
            assertTrue(messages.get(0).getExpectedValue().isPresent());
            assertTrue(messages.get(0).getExpectedValue().get().contains("NOTE"));
        }
        
        @Test
        @DisplayName("should accept valid types")
        void shouldAcceptValidTypes() {
            // Given
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .type(AdmonitionBlock.TypeConfig.builder()
                    .required(true)
                    .allowed(List.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION"))
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Type Occurrences Validation")
    class TypeOccurrencesValidation {
        
        @Test
        @DisplayName("should validate type occurrences")
        void shouldValidateTypeOccurrences() {
            // Given
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .noteOccurrence(AdmonitionBlock.TypeOccurrenceConfig.builder()
                    .max(2)
                    .severity(Severity.WARN)
                    .build())
                .build();
            
            // When - validate three NOTE blocks
            validator.validate(mockBlock, config, context);
            validator.validate(mockBlock, config, context);
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.NOTE.max", messages.get(0).getRuleId());
            assertEquals(Severity.WARN, messages.get(0).getSeverity());
        }
        
        @Test
        @DisplayName("should track different admonition types separately")
        void shouldTrackDifferentTypesSeparately() {
            // Given
            StructuralNode noteBlock = mock(StructuralNode.class);
            when(noteBlock.getStyle()).thenReturn("NOTE");
            when(noteBlock.getDocument()).thenReturn(mockDocument);
            
            StructuralNode tipBlock = mock(StructuralNode.class);
            when(tipBlock.getStyle()).thenReturn("TIP");
            when(tipBlock.getDocument()).thenReturn(mockDocument);
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .noteOccurrence(AdmonitionBlock.TypeOccurrenceConfig.builder()
                    .max(1)
                    .build())
                .tipOccurrence(AdmonitionBlock.TypeOccurrenceConfig.builder()
                    .max(1)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> noteMessages1 = validator.validate(noteBlock, config, context);
            List<ValidationMessage> tipMessages1 = validator.validate(tipBlock, config, context);
            List<ValidationMessage> noteMessages2 = validator.validate(noteBlock, config, context);
            
            // Then
            assertTrue(noteMessages1.isEmpty());
            assertTrue(tipMessages1.isEmpty());
            assertEquals(1, noteMessages2.size());
            assertEquals("admonition.NOTE.max", noteMessages2.get(0).getRuleId());
        }
    }
    
    @Nested
    @DisplayName("Admonition Type Detection")
    class AdmonitionTypeDetection {
        
        @Test
        @DisplayName("should detect admonition type from style")
        void shouldDetectTypeFromStyle() {
            // Given
            when(mockBlock.getStyle()).thenReturn("warning");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            validator.validate(mockBlock, config, context);
            
            // Then
            verify(mockBlock).getStyle();
        }
        
        @Test
        @DisplayName("should detect admonition type from role as fallback")
        void shouldDetectTypeFromRole() {
            // Given
            when(mockBlock.getStyle()).thenReturn(null);
            when(mockBlock.getAttribute("role")).thenReturn("caution");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            validator.validate(mockBlock, config, context);
            
            // Then
            verify(mockBlock).getAttribute("role");
        }
        
        @Test
        @DisplayName("should convert type to uppercase")
        void shouldConvertTypeToUppercase() {
            // Given
            when(mockBlock.getStyle()).thenReturn("note");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .noteOccurrence(AdmonitionBlock.TypeOccurrenceConfig.builder()
                    .max(1)
                    .build())
                .build();
            
            // When
            validator.validate(mockBlock, config, context);
            validator.validate(mockBlock, config, context);
            
            // Then - should track as NOTE (uppercase)
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            assertEquals(1, messages.size());
        }
    }
    
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {
        
        @Test
        @DisplayName("should handle null content")
        void shouldHandleNullContent() {
            // Given
            when(mockBlock.getContent()).thenReturn(null);
            when(mockBlock.getBlocks()).thenReturn(null);
            when(mockBlock.getStyle()).thenReturn("IMPORTANT");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .content(AdmonitionBlock.ContentConfig.builder()
                    .minLength(10)
                    .build())
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("admonition.content.minLength", messages.get(0).getRuleId());
        }
        
        @Test
        @DisplayName("should handle empty configuration")
        void shouldHandleEmptyConfiguration() {
            // Given
            when(mockBlock.getStyle()).thenReturn("NOTE");
            
            AdmonitionBlock config = AdmonitionBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
}