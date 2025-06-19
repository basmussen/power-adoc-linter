package com.example.linter.config.blocks;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class VerseBlockTest {
    
    @Test
    void testBuilder() {
        VerseBlock.AuthorRule authorRule = VerseBlock.AuthorRule.builder()
                .defaultValue("Carl Sandburg")
                .required(true)
                .minLength(3)
                .maxLength(50)
                .pattern("^[A-Z][a-zA-Z\\s\\.]+$")
                .build();
                
        VerseBlock.AttributionRule attributionRule = VerseBlock.AttributionRule.builder()
                .defaultValue("Fog")
                .required(false)
                .minLength(5)
                .maxLength(100)
                .pattern("^[A-Za-z0-9\\s,\\.]+$")
                .build();
                
        VerseBlock.ContentRule contentRule = VerseBlock.ContentRule.builder()
                .required(true)
                .minLength(20)
                .maxLength(500)
                .pattern(".*\\n.*")
                .build();
        
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.WARN)
                .author(authorRule)
                .attribution(attributionRule)
                .content(contentRule)
                .build();
        
        assertEquals(Severity.WARN, verse.getSeverity());
        
        assertNotNull(verse.getAuthor());
        assertEquals("Carl Sandburg", verse.getAuthor().getDefaultValue());
        assertTrue(verse.getAuthor().isRequired());
        assertEquals(3, verse.getAuthor().getMinLength());
        assertEquals(50, verse.getAuthor().getMaxLength());
        assertNotNull(verse.getAuthor().getPattern());
        
        assertNotNull(verse.getAttribution());
        assertEquals("Fog", verse.getAttribution().getDefaultValue());
        assertFalse(verse.getAttribution().isRequired());
        assertEquals(5, verse.getAttribution().getMinLength());
        assertEquals(100, verse.getAttribution().getMaxLength());
        assertNotNull(verse.getAttribution().getPattern());
        
        assertNotNull(verse.getContent());
        assertTrue(verse.getContent().isRequired());
        assertEquals(20, verse.getContent().getMinLength());
        assertEquals(500, verse.getContent().getMaxLength());
        assertNotNull(verse.getContent().getPattern());
    }
    
    @Test
    void testPatternStringConstructor() {
        VerseBlock.AuthorRule authorRule = VerseBlock.AuthorRule.builder()
                .pattern("^[A-Z].*")
                .build();
                
        VerseBlock.AttributionRule attributionRule = VerseBlock.AttributionRule.builder()
                .pattern(Pattern.compile("[0-9]+"))
                .build();
        
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.ERROR)
                .author(authorRule)
                .attribution(attributionRule)
                .build();
        
        assertNotNull(verse.getAuthor().getPattern());
        assertEquals("^[A-Z].*", verse.getAuthor().getPattern().pattern());
        assertNotNull(verse.getAttribution().getPattern());
        assertEquals("[0-9]+", verse.getAttribution().getPattern().pattern());
    }
    
    @Test
    void testNullPatterns() {
        VerseBlock.AuthorRule authorRule = VerseBlock.AuthorRule.builder()
                .pattern((String) null)
                .build();
                
        VerseBlock.AttributionRule attributionRule = VerseBlock.AttributionRule.builder()
                .pattern((Pattern) null)
                .build();
        
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.INFO)
                .author(authorRule)
                .attribution(attributionRule)
                .build();
        
        assertNull(verse.getAuthor().getPattern());
        assertNull(verse.getAttribution().getPattern());
    }
    
    @Test
    void testEqualsAndHashCode() {
        VerseBlock.AuthorRule authorRule1 = VerseBlock.AuthorRule.builder()
                .defaultValue("Author1")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();
                
        VerseBlock.AuthorRule authorRule2 = VerseBlock.AuthorRule.builder()
                .defaultValue("Author1")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();
                
        VerseBlock.AuthorRule authorRule3 = VerseBlock.AuthorRule.builder()
                .defaultValue("Author2")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();
        
        VerseBlock verse1 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author(authorRule1)
                .build();
        
        VerseBlock verse2 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author(authorRule2)
                .build();
        
        VerseBlock verse3 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author(authorRule3)
                .build();
        
        assertEquals(verse1, verse2);
        assertNotEquals(verse1, verse3);
        assertEquals(verse1.hashCode(), verse2.hashCode());
        assertNotEquals(verse1.hashCode(), verse3.hashCode());
    }
    
    @Test
    void testRequiredSeverity() {
        assertThrows(NullPointerException.class, () -> {
            VerseBlock.builder().build();
        });
    }
    
    @Test
    void testInnerClassEqualsAndHashCode() {
        VerseBlock.AuthorRule author1 = VerseBlock.AuthorRule.builder()
                .defaultValue("Test")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();
                
        VerseBlock.AuthorRule author2 = VerseBlock.AuthorRule.builder()
                .defaultValue("Test")
                .required(true)
                .minLength(5)
                .maxLength(50)
                .pattern("^[A-Z].*")
                .build();
                
        VerseBlock.AttributionRule attr1 = VerseBlock.AttributionRule.builder()
                .defaultValue("Source")
                .required(false)
                .build();
                
        VerseBlock.AttributionRule attr2 = VerseBlock.AttributionRule.builder()
                .defaultValue("Source")
                .required(false)
                .build();
                
        VerseBlock.ContentRule content1 = VerseBlock.ContentRule.builder()
                .required(true)
                .minLength(10)
                .build();
                
        VerseBlock.ContentRule content2 = VerseBlock.ContentRule.builder()
                .required(true)
                .minLength(10)
                .build();
        
        assertEquals(author1, author2);
        assertEquals(author1.hashCode(), author2.hashCode());
        
        assertEquals(attr1, attr2);
        assertEquals(attr1.hashCode(), attr2.hashCode());
        
        assertEquals(content1, content2);
        assertEquals(content1.hashCode(), content2.hashCode());
    }
}