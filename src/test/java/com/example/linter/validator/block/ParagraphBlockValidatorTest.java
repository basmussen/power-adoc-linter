package com.example.linter.validator.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.rule.LineConfig;
import com.example.linter.validator.ValidationMessage;

@DisplayName("ParagraphBlockValidator")
class ParagraphBlockValidatorTest {
    
    private ParagraphBlockValidator validator;
    private BlockValidationContext context;
    private StructuralNode mockBlock;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new ParagraphBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(StructuralNode.class);
    }
    
    @Test
    @DisplayName("should return PARAGRAPH as supported type")
    void shouldReturnParagraphAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.PARAGRAPH, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when no line config")
        void shouldReturnEmptyListWhenNoLineConfig() {
            // Given
            ParagraphBlock config = ParagraphBlock.builder()
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("Some content");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should validate minimum lines")
        void shouldValidateMinimumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(3)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("paragraph.lines.min", msg.getRuleId());
            assertEquals("Paragraph has too few lines", msg.getMessage());
            assertEquals("2", msg.getActualValue().orElse(null));
            assertEquals("At least 3 lines", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum lines")
        void shouldValidateMaximumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .max(2)
                .severity(Severity.WARN)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("paragraph.lines.max", msg.getRuleId());
            assertEquals("Paragraph has too many lines", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At most 2 lines", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should pass when lines within range")
        void shouldPassWhenLinesWithinRange() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(2)
                .max(5)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("Line 1\nLine 2\nLine 3");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should count only non-empty lines")
        void shouldCountOnlyNonEmptyLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(2)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("Line 1\n\n  \nLine 2");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty()); // 2 non-empty lines, meets minimum
        }
        
        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(1)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn("");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("0", messages.get(0).getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should handle null content")
        void shouldHandleNullContent() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(1)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            when(mockBlock.getContent()).thenReturn(null);
            when(mockBlock.getBlocks()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            assertEquals("0", messages.get(0).getActualValue().orElse(null));
        }
        
        @Test
        @DisplayName("should get content from blocks when direct content is null")
        void shouldGetContentFromBlocksWhenDirectContentIsNull() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(1)
                .severity(Severity.ERROR)
                .build();
            ParagraphBlock config = ParagraphBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            
            StructuralNode childBlock = mock(StructuralNode.class);
            when(childBlock.getContent()).thenReturn("Child content");
            when(mockBlock.getContent()).thenReturn(null);
            when(mockBlock.getBlocks()).thenReturn(Arrays.asList(childBlock));
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty()); // Has content from child
        }
    }
}