package com.example.linter.config.blocks;

import com.example.linter.config.LineRule;
import com.example.linter.config.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ListingBlock")
class ListingBlockTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build ListingBlock with all attributes")
        void shouldBuildListingBlockWithAllAttributes() {
            // Given
            ListingBlock.LanguageRule languageRule = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(Arrays.asList("java", "python", "javascript", "yaml"))
                    .severity(Severity.ERROR)
                    .build();
                    
            LineRule linesRule = LineRule.builder()
                    .min(3)
                    .max(100)
                    .build();
                    
            ListingBlock.TitleRule titleRule = ListingBlock.TitleRule.builder()
                    .required(true)
                    .pattern("^(Example|Beispiel|Listing)\\s+\\d+")
                    .severity(Severity.WARN)
                    .build();
                    
            ListingBlock.CalloutsRule calloutsRule = ListingBlock.CalloutsRule.builder()
                    .allowed(true)
                    .max(10)
                    .severity(Severity.INFO)
                    .build();
            
            // When
            ListingBlock listing = ListingBlock.builder()
                    .name("api-examples")
                    .severity(Severity.ERROR)
                    .language(languageRule)
                    .lines(linesRule)
                    .title(titleRule)
                    .callouts(calloutsRule)
                    .build();
            
            // Then
            assertEquals("api-examples", listing.getName());
            assertEquals(Severity.ERROR, listing.getSeverity());
            
            assertNotNull(listing.getLanguage());
            assertTrue(listing.getLanguage().isRequired());
            assertEquals(Arrays.asList("java", "python", "javascript", "yaml"), 
                        listing.getLanguage().getAllowed());
            assertEquals(Severity.ERROR, listing.getLanguage().getSeverity());
            
            assertNotNull(listing.getLines());
            assertEquals(3, listing.getLines().min());
            assertEquals(100, listing.getLines().max());
            
            assertNotNull(listing.getTitle());
            assertTrue(listing.getTitle().isRequired());
            assertNotNull(listing.getTitle().getPattern());
            assertEquals(Severity.WARN, listing.getTitle().getSeverity());
            
            assertNotNull(listing.getCallouts());
            assertTrue(listing.getCallouts().isAllowed());
            assertEquals(10, listing.getCallouts().getMax());
            assertEquals(Severity.INFO, listing.getCallouts().getSeverity());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ListingBlock.builder().build();
            });
        }
    }
    
    @Nested
    @DisplayName("LanguageRule Tests")
    class LanguageRuleTests {
        
        @Test
        @DisplayName("should create LanguageRule with allowed languages")
        void shouldCreateLanguageRuleWithAllowedLanguages() {
            // Given
            List<String> allowedLanguages = Arrays.asList("java", "kotlin", "scala");
            
            // When
            ListingBlock.LanguageRule languageRule = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(allowedLanguages)
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertTrue(languageRule.isRequired());
            assertEquals(allowedLanguages, languageRule.getAllowed());
            assertEquals(Severity.ERROR, languageRule.getSeverity());
        }
        
        @Test
        @DisplayName("should handle empty allowed list")
        void shouldHandleEmptyAllowedList() {
            // When
            ListingBlock.LanguageRule languageRule = ListingBlock.LanguageRule.builder()
                    .required(false)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertFalse(languageRule.isRequired());
            assertTrue(languageRule.getAllowed().isEmpty());
        }
        
        @Test
        @DisplayName("should require severity for LanguageRule")
        void shouldRequireSeverityForLanguageRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ListingBlock.LanguageRule.builder()
                        .required(true)
                        .build();
            });
        }
    }
    
    @Nested
    @DisplayName("TitleRule Tests")
    class TitleRuleTests {
        
        @Test
        @DisplayName("should create TitleRule with string pattern")
        void shouldCreateTitleRuleWithStringPattern() {
            // Given & When
            ListingBlock.TitleRule titleRule = ListingBlock.TitleRule.builder()
                    .required(true)
                    .pattern("^Listing \\d+:")
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertTrue(titleRule.isRequired());
            assertNotNull(titleRule.getPattern());
            assertEquals("^Listing \\d+:", titleRule.getPattern().pattern());
            assertEquals(Severity.ERROR, titleRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create TitleRule with Pattern object")
        void shouldCreateTitleRuleWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^Example.*");
            
            // When
            ListingBlock.TitleRule titleRule = ListingBlock.TitleRule.builder()
                    .required(false)
                    .pattern(pattern)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertFalse(titleRule.isRequired());
            assertEquals(pattern, titleRule.getPattern());
            assertEquals(Severity.WARN, titleRule.getSeverity());
        }
        
        @Test
        @DisplayName("should require severity for TitleRule")
        void shouldRequireSeverityForTitleRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ListingBlock.TitleRule.builder()
                        .required(true)
                        .pattern("test")
                        .build();
            });
        }
    }
    
    @Nested
    @DisplayName("CalloutsRule Tests")
    class CalloutsRuleTests {
        
        @Test
        @DisplayName("should create CalloutsRule with max limit")
        void shouldCreateCalloutsRuleWithMaxLimit() {
            // Given & When
            ListingBlock.CalloutsRule calloutsRule = ListingBlock.CalloutsRule.builder()
                    .allowed(true)
                    .max(15)
                    .severity(Severity.INFO)
                    .build();
            
            // Then
            assertTrue(calloutsRule.isAllowed());
            assertEquals(15, calloutsRule.getMax());
            assertEquals(Severity.INFO, calloutsRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create CalloutsRule without max limit")
        void shouldCreateCalloutsRuleWithoutMaxLimit() {
            // Given & When
            ListingBlock.CalloutsRule calloutsRule = ListingBlock.CalloutsRule.builder()
                    .allowed(false)
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertFalse(calloutsRule.isAllowed());
            assertNull(calloutsRule.getMax());
            assertEquals(Severity.ERROR, calloutsRule.getSeverity());
        }
        
        @Test
        @DisplayName("should require severity for CalloutsRule")
        void shouldRequireSeverityForCalloutsRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                ListingBlock.CalloutsRule.builder()
                        .allowed(true)
                        .max(5)
                        .build();
            });
        }
    }
    
    @Nested
    @DisplayName("Equals and HashCode Tests")
    class EqualsHashCodeTests {
        
        @Test
        @DisplayName("should correctly implement equals and hashCode")
        void shouldCorrectlyImplementEqualsAndHashCode() {
            // Given
            ListingBlock.LanguageRule lang1 = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(Arrays.asList("java", "python"))
                    .severity(Severity.ERROR)
                    .build();
                    
            ListingBlock.LanguageRule lang2 = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(Arrays.asList("java", "python"))
                    .severity(Severity.ERROR)
                    .build();
                    
            ListingBlock.TitleRule title1 = ListingBlock.TitleRule.builder()
                    .required(true)
                    .pattern("^Example.*")
                    .severity(Severity.WARN)
                    .build();
                    
            ListingBlock.TitleRule title2 = ListingBlock.TitleRule.builder()
                    .required(true)
                    .pattern("^Example.*")
                    .severity(Severity.WARN)
                    .build();
                    
            // When
            ListingBlock listing1 = ListingBlock.builder()
                    .severity(Severity.ERROR)
                    .language(lang1)
                    .title(title1)
                    .build();
                    
            ListingBlock listing2 = ListingBlock.builder()
                    .severity(Severity.ERROR)
                    .language(lang2)
                    .title(title2)
                    .build();
                    
            ListingBlock listing3 = ListingBlock.builder()
                    .severity(Severity.WARN)
                    .language(lang1)
                    .title(title1)
                    .build();
            
            // Then
            assertEquals(listing1, listing2);
            assertNotEquals(listing1, listing3);
            assertEquals(listing1.hashCode(), listing2.hashCode());
            assertNotEquals(listing1.hashCode(), listing3.hashCode());
        }
        
        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            ListingBlock.LanguageRule lang1 = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(Arrays.asList("java"))
                    .severity(Severity.ERROR)
                    .build();
                    
            ListingBlock.LanguageRule lang2 = ListingBlock.LanguageRule.builder()
                    .required(true)
                    .allowed(Arrays.asList("java"))
                    .severity(Severity.ERROR)
                    .build();
                    
            ListingBlock.TitleRule title1 = ListingBlock.TitleRule.builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();
                    
            ListingBlock.TitleRule title2 = ListingBlock.TitleRule.builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();
                    
            ListingBlock.CalloutsRule callouts1 = ListingBlock.CalloutsRule.builder()
                    .allowed(true)
                    .max(5)
                    .severity(Severity.WARN)
                    .build();
                    
            ListingBlock.CalloutsRule callouts2 = ListingBlock.CalloutsRule.builder()
                    .allowed(true)
                    .max(5)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertEquals(lang1, lang2);
            assertEquals(lang1.hashCode(), lang2.hashCode());
            
            assertEquals(title1, title2);
            assertEquals(title1.hashCode(), title2.hashCode());
            
            assertEquals(callouts1, callouts2);
            assertEquals(callouts1.hashCode(), callouts2.hashCode());
        }
    }
}