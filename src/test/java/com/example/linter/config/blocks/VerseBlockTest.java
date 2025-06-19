package com.example.linter.config.blocks;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class VerseBlockTest {
    
    @Test
    void testBuilder() {
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.WARN)
                .author("Carl Sandburg")
                .authorRequired(true)
                .authorMinLength(3)
                .authorMaxLength(50)
                .authorPattern("^[A-Z][a-zA-Z\\s\\.]+$")
                .attribution("Fog")
                .attributionRequired(false)
                .attributionMinLength(5)
                .attributionMaxLength(100)
                .attributionPattern("^[A-Za-z0-9\\s,\\.]+$")
                .contentRequired(true)
                .contentMinLength(20)
                .contentMaxLength(500)
                .contentPattern(".*\\n.*")
                .build();
        
        assertEquals(Severity.WARN, verse.getSeverity());
        assertEquals("Carl Sandburg", verse.getAuthor());
        assertTrue(verse.isAuthorRequired());
        assertEquals(3, verse.getAuthorMinLength());
        assertEquals(50, verse.getAuthorMaxLength());
        assertNotNull(verse.getAuthorPattern());
        
        assertEquals("Fog", verse.getAttribution());
        assertFalse(verse.isAttributionRequired());
        assertEquals(5, verse.getAttributionMinLength());
        assertEquals(100, verse.getAttributionMaxLength());
        assertNotNull(verse.getAttributionPattern());
        
        assertTrue(verse.isContentRequired());
        assertEquals(20, verse.getContentMinLength());
        assertEquals(500, verse.getContentMaxLength());
        assertNotNull(verse.getContentPattern());
    }
    
    @Test
    void testPatternStringConstructor() {
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.ERROR)
                .authorPattern("^[A-Z].*")
                .attributionPattern(Pattern.compile("[0-9]+"))
                .build();
        
        assertNotNull(verse.getAuthorPattern());
        assertEquals("^[A-Z].*", verse.getAuthorPattern().pattern());
        assertNotNull(verse.getAttributionPattern());
        assertEquals("[0-9]+", verse.getAttributionPattern().pattern());
    }
    
    @Test
    void testNullPatterns() {
        VerseBlock verse = VerseBlock.builder()
                .severity(Severity.INFO)
                .authorPattern((String) null)
                .attributionPattern((Pattern) null)
                .build();
        
        assertNull(verse.getAuthorPattern());
        assertNull(verse.getAttributionPattern());
    }
    
    @Test
    void testEqualsAndHashCode() {
        VerseBlock verse1 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author("Author1")
                .authorRequired(true)
                .authorMinLength(5)
                .authorMaxLength(50)
                .authorPattern("^[A-Z].*")
                .build();
        
        VerseBlock verse2 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author("Author1")
                .authorRequired(true)
                .authorMinLength(5)
                .authorMaxLength(50)
                .authorPattern("^[A-Z].*")
                .build();
        
        VerseBlock verse3 = VerseBlock.builder()
                .severity(Severity.WARN)
                .author("Author2")
                .authorRequired(true)
                .authorMinLength(5)
                .authorMaxLength(50)
                .authorPattern("^[A-Z].*")
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
}