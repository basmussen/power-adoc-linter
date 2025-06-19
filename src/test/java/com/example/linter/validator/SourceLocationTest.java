package com.example.linter.validator;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SourceLocation")
class SourceLocationTest {

    @Nested
    @DisplayName("Builder")
    class BuilderTest {
        
        @Test
        @DisplayName("should create location with all fields")
        void shouldCreateLocationWithAllFields() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .startLine(10)
                .startColumn(5)
                .endLine(12)
                .endColumn(15)
                .sourceLine("= Test Document")
                .build();
            
            assertEquals("test.adoc", location.getFilename());
            assertEquals(10, location.getStartLine());
            assertEquals(5, location.getStartColumn());
            assertEquals(12, location.getEndLine());
            assertEquals(15, location.getEndColumn());
            assertEquals("= Test Document", location.getSourceLine());
        }
        
        @Test
        @DisplayName("should create single line location")
        void shouldCreateSingleLineLocation() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(5)
                .columns(10, 20)
                .build();
            
            assertEquals(5, location.getStartLine());
            assertEquals(5, location.getEndLine());
            assertEquals(10, location.getStartColumn());
            assertEquals(20, location.getEndColumn());
            assertFalse(location.isMultiLine());
        }
        
        @Test
        @DisplayName("should require filename")
        void shouldRequireFilename() {
            assertThrows(NullPointerException.class, () -> 
                SourceLocation.builder().build()
            );
        }
    }

    @Nested
    @DisplayName("Formatting")
    class FormattingTest {
        
        @Test
        @DisplayName("should format single line with column range")
        void shouldFormatSingleLineWithColumnRange() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .columns(5, 15)
                .build();
            
            assertEquals("test.adoc:10:5-15", location.formatLocation());
        }
        
        @Test
        @DisplayName("should format single line with single column")
        void shouldFormatSingleLineWithSingleColumn() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .startColumn(5)
                .endColumn(5)
                .build();
            
            assertEquals("test.adoc:10:5", location.formatLocation());
        }
        
        @Test
        @DisplayName("should format multi-line location")
        void shouldFormatMultiLineLocation() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .startLine(10)
                .endLine(15)
                .build();
            
            assertEquals("test.adoc:10-15", location.formatLocation());
            assertTrue(location.isMultiLine());
        }
        
        @Test
        @DisplayName("should format line only location")
        void shouldFormatLineOnlyLocation() {
            SourceLocation location = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .build();
            
            assertEquals("test.adoc:10", location.formatLocation());
        }
    }

    @Nested
    @DisplayName("Equals and HashCode")
    class EqualsHashCodeTest {
        
        @Test
        @DisplayName("should be equal for same values")
        void shouldBeEqualForSameValues() {
            SourceLocation location1 = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .build();
            
            SourceLocation location2 = SourceLocation.builder()
                .filename("test.adoc")
                .line(10)
                .build();
            
            assertEquals(location1, location2);
            assertEquals(location1.hashCode(), location2.hashCode());
        }
        
        @Test
        @DisplayName("should not be equal for different filenames")
        void shouldNotBeEqualForDifferentFilenames() {
            SourceLocation location1 = SourceLocation.builder()
                .filename("test1.adoc")
                .line(10)
                .build();
            
            SourceLocation location2 = SourceLocation.builder()
                .filename("test2.adoc")
                .line(10)
                .build();
            
            assertNotEquals(location1, location2);
        }
    }
}