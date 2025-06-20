package com.example.linter.validator.block;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.rule.LineConfig;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.Block;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("ListingBlockValidator")
class ListingBlockValidatorTest {
    
    private ListingBlockValidator validator;
    private BlockValidationContext context;
    private Block mockBlock;
    private Section mockSection;
    
    @BeforeEach
    void setUp() {
        validator = new ListingBlockValidator();
        mockSection = mock(Section.class);
        context = new BlockValidationContext(mockSection, "test.adoc");
        mockBlock = mock(Block.class);
    }
    
    @Test
    @DisplayName("should return LISTING as supported type")
    void shouldReturnListingAsSupportedType() {
        // Given/When
        BlockType type = validator.getSupportedType();
        
        // Then
        assertEquals(BlockType.LISTING, type);
    }
    
    @Nested
    @DisplayName("validate")
    class Validate {
        
        @Test
        @DisplayName("should return empty list when block is not Block instance")
        void shouldReturnEmptyListWhenNotBlockInstance() {
            // Given
            StructuralNode notABlock = mock(StructuralNode.class);
            ListingBlock config = ListingBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(notABlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
        
        @Test
        @DisplayName("should return empty list when no validations configured")
        void shouldReturnEmptyListWhenNoValidationsConfigured() {
            // Given
            ListingBlock config = ListingBlock.builder()
                .severity(Severity.ERROR)
                .build();
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("language validation")
    class LanguageValidation {
        
        @Test
        @DisplayName("should validate required language")
        void shouldValidateRequiredLanguage() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
                .language(languageConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("language")).thenReturn(false);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.ERROR, msg.getSeverity());
            assertEquals("listing.language.required", msg.getRuleId());
            assertEquals("Code listing must specify a language", msg.getMessage());
            assertEquals("No language", msg.getActualValue().orElse(null));
            assertEquals("Language required", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate allowed languages")
        void shouldValidateAllowedLanguages() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig.builder()
                .allowed(Arrays.asList("java", "python", "javascript"))
                .severity(Severity.WARN)
                .build();
            ListingBlock config = ListingBlock.builder()
                .language(languageConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("ruby");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.language.allowed", msg.getRuleId());
            assertEquals("Code listing uses unsupported language", msg.getMessage());
            assertEquals("ruby", msg.getActualValue().orElse(null));
            assertEquals("Allowed languages: [java, python, javascript]", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should pass when language is allowed")
        void shouldPassWhenLanguageIsAllowed() {
            // Given
            ListingBlock.LanguageConfig languageConfig = ListingBlock.LanguageConfig.builder()
                .allowed(Arrays.asList("java", "python"))
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
                .language(languageConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("java");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("title validation")
    class TitleValidation {
        
        @Test
        @DisplayName("should validate required title")
        void shouldValidateRequiredTitle() {
            // Given
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig.builder()
                .required(true)
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
                .title(titleConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getTitle()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.title.required", msg.getRuleId());
            assertEquals("Code listing must have a title", msg.getMessage());
        }
        
        @Test
        @DisplayName("should validate title pattern")
        void shouldValidateTitlePattern() {
            // Given
            ListingBlock.TitleConfig titleConfig = ListingBlock.TitleConfig.builder()
                .pattern(Pattern.compile("^Listing \\d+:.*"))
                .severity(Severity.INFO)
                .build();
            ListingBlock config = ListingBlock.builder()
                .title(titleConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getTitle()).thenReturn("Code Example");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("listing.title.pattern", msg.getRuleId());
            assertEquals("Code listing title does not match required pattern", msg.getMessage());
            assertEquals("Code Example", msg.getActualValue().orElse(null));
            assertEquals("Pattern: ^Listing \\d+:.*", msg.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("callouts validation")
    class CalloutsValidation {
        
        @Test
        @DisplayName("should validate callouts not allowed")
        void shouldValidateCalloutsNotAllowed() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig.builder()
                .allowed(false)
                .severity(Severity.WARN)
                .build();
            ListingBlock config = ListingBlock.builder()
                .callouts(calloutsConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.callouts.notAllowed", msg.getRuleId());
            assertEquals("Code listing should not have callouts", msg.getMessage());
            assertEquals("Has callouts", msg.getActualValue().orElse(null));
            assertEquals("No callouts allowed", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should allow callouts when allowed is true")
        void shouldAllowCalloutsWhenAllowedIsTrue() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig.builder()
                .allowed(true)
                .severity(Severity.WARN)
                .build();
            ListingBlock config = ListingBlock.builder()
                .callouts(calloutsConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("public class Test { // <1>\n    // code\n}");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty()); // Callouts allowed
        }
        
        @Test
        @DisplayName("should validate maximum callout count")
        void shouldValidateMaximumCalloutCount() {
            // Given
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig.builder()
                .max(2)
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
                .callouts(calloutsConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("code // <1>\nmore // <2>\nagain // <3>");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals("listing.callouts.max", msg.getRuleId());
            assertEquals("Code listing has too many callouts", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At most 2 callouts", msg.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("lines validation")
    class LinesValidation {
        
        @Test
        @DisplayName("should validate minimum lines")
        void shouldValidateMinimumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(5)
                .severity(Severity.INFO)
                .build();
            ListingBlock config = ListingBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn("line1\nline2\nline3");
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.INFO, msg.getSeverity());
            assertEquals("listing.lines.min", msg.getRuleId());
            assertEquals("Code listing has too few lines", msg.getMessage());
            assertEquals("3", msg.getActualValue().orElse(null));
            assertEquals("At least 5 lines", msg.getExpectedValue().orElse(null));
        }
        
        @Test
        @DisplayName("should validate maximum lines")
        void shouldValidateMaximumLines() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .max(50)
                .severity(Severity.WARN)
                .build();
            ListingBlock config = ListingBlock.builder()
                .lines(lineConfig)
                .severity(Severity.ERROR)
                .build();
            
            // Create content with 51 lines
            StringBuilder content = new StringBuilder();
            for (int i = 1; i <= 51; i++) {
                content.append("line ").append(i).append("\n");
            }
            when(mockBlock.getContent()).thenReturn(content.toString());
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(1, messages.size());
            ValidationMessage msg = messages.get(0);
            assertEquals(Severity.WARN, msg.getSeverity());
            assertEquals("listing.lines.max", msg.getRuleId());
            assertEquals("Code listing has too many lines", msg.getMessage());
            assertEquals("51", msg.getActualValue().orElse(null));
            assertEquals("At most 50 lines", msg.getExpectedValue().orElse(null));
        }
    }
    
    @Nested
    @DisplayName("complex validation scenarios")
    class ComplexScenarios {
        
        @Test
        @DisplayName("should validate multiple rules together")
        void shouldValidateMultipleRules() {
            // Given
            ListingBlock config = ListingBlock.builder()
                .language(ListingBlock.LanguageConfig.builder()
                    .required(true)
                    .allowed(Arrays.asList("java", "python"))
                    .severity(Severity.ERROR)
                    .build())
                .title(ListingBlock.TitleConfig.builder()
                    .required(true)
                    .severity(Severity.WARN)
                    .build())
                .lines(LineConfig.builder()
                    .max(10)
                    .severity(Severity.INFO)
                    .build())
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.hasAttribute("language")).thenReturn(true);
            when(mockBlock.getAttribute("language")).thenReturn("javascript"); // Not allowed
            when(mockBlock.getTitle()).thenReturn(null); // Missing
            when(mockBlock.getContent()).thenReturn("line1\nline2\nline3\nline4\nline5\nline6\nline7\nline8\nline9\nline10\nline11"); // Too long
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertEquals(3, messages.size());
            assertTrue(messages.stream().anyMatch(m -> "listing.language.allowed".equals(m.getRuleId())));
            assertTrue(messages.stream().anyMatch(m -> "listing.title.required".equals(m.getRuleId())));
            assertTrue(messages.stream().anyMatch(m -> "listing.lines.max".equals(m.getRuleId())));
        }
        
        @Test
        @DisplayName("should handle empty content")
        void shouldHandleEmptyContent() {
            // Given
            LineConfig lineConfig = LineConfig.builder()
                .min(1)
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
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
            ListingBlock.CalloutsConfig calloutsConfig = ListingBlock.CalloutsConfig.builder()
                .allowed(false)
                .severity(Severity.ERROR)
                .build();
            ListingBlock config = ListingBlock.builder()
                .callouts(calloutsConfig)
                .severity(Severity.ERROR)
                .build();
            
            when(mockBlock.getContent()).thenReturn(null);
            
            // When
            List<ValidationMessage> messages = validator.validate(mockBlock, config, context);
            
            // Then
            assertTrue(messages.isEmpty()); // No content means no callouts
        }
    }
}