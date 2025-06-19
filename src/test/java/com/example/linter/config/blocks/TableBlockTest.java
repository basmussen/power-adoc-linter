package com.example.linter.config.blocks;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TableBlock")
class TableBlockTest {
    
    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {
        
        @Test
        @DisplayName("should build TableBlock with all attributes")
        void shouldBuildTableBlockWithAllAttributes() {
            // Given
            TableBlock.DimensionRule columnsRule = TableBlock.DimensionRule.builder()
                    .min(2)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.DimensionRule rowsRule = TableBlock.DimensionRule.builder()
                    .min(1)
                    .max(100)
                    .severity(Severity.WARN)
                    .build();
                    
            TableBlock.HeaderRule headerRule = TableBlock.HeaderRule.builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.CaptionRule captionRule = TableBlock.CaptionRule.builder()
                    .required(true)
                    .pattern("^Table \\d+:")
                    .minLength(10)
                    .maxLength(200)
                    .severity(Severity.WARN)
                    .build();
                    
            TableBlock.FormatRule formatRule = TableBlock.FormatRule.builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();
            
            // When
            TableBlock table = TableBlock.builder()
                    .name("data-tables")
                    .severity(Severity.ERROR)
                    .columns(columnsRule)
                    .rows(rowsRule)
                    .header(headerRule)
                    .caption(captionRule)
                    .format(formatRule)
                    .build();
            
            // Then
            assertEquals("data-tables", table.getName());
            assertEquals(Severity.ERROR, table.getSeverity());
            
            assertNotNull(table.getColumns());
            assertEquals(2, table.getColumns().getMin());
            assertEquals(10, table.getColumns().getMax());
            assertEquals(Severity.ERROR, table.getColumns().getSeverity());
            
            assertNotNull(table.getRows());
            assertEquals(1, table.getRows().getMin());
            assertEquals(100, table.getRows().getMax());
            assertEquals(Severity.WARN, table.getRows().getSeverity());
            
            assertNotNull(table.getHeader());
            assertTrue(table.getHeader().isRequired());
            assertNotNull(table.getHeader().getPattern());
            assertEquals(Severity.ERROR, table.getHeader().getSeverity());
            
            assertNotNull(table.getCaption());
            assertTrue(table.getCaption().isRequired());
            assertNotNull(table.getCaption().getPattern());
            assertEquals(10, table.getCaption().getMinLength());
            assertEquals(200, table.getCaption().getMaxLength());
            assertEquals(Severity.WARN, table.getCaption().getSeverity());
            
            assertNotNull(table.getFormat());
            assertEquals("grid", table.getFormat().getStyle());
            assertTrue(table.getFormat().getBorders());
            assertEquals(Severity.INFO, table.getFormat().getSeverity());
        }
        
        @Test
        @DisplayName("should require severity")
        void shouldRequireSeverity() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                TableBlock.builder().build();
            });
        }
    }
    
    @Nested
    @DisplayName("DimensionRule Tests")
    class DimensionRuleTests {
        
        @Test
        @DisplayName("should create DimensionRule with min and max")
        void shouldCreateDimensionRuleWithMinAndMax() {
            // Given & When
            TableBlock.DimensionRule dimensionRule = TableBlock.DimensionRule.builder()
                    .min(3)
                    .max(15)
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertEquals(3, dimensionRule.getMin());
            assertEquals(15, dimensionRule.getMax());
            assertEquals(Severity.ERROR, dimensionRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create DimensionRule with only min")
        void shouldCreateDimensionRuleWithOnlyMin() {
            // When
            TableBlock.DimensionRule dimensionRule = TableBlock.DimensionRule.builder()
                    .min(5)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertEquals(5, dimensionRule.getMin());
            assertNull(dimensionRule.getMax());
            assertEquals(Severity.WARN, dimensionRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create DimensionRule with only max")
        void shouldCreateDimensionRuleWithOnlyMax() {
            // When
            TableBlock.DimensionRule dimensionRule = TableBlock.DimensionRule.builder()
                    .max(20)
                    .severity(Severity.INFO)
                    .build();
            
            // Then
            assertNull(dimensionRule.getMin());
            assertEquals(20, dimensionRule.getMax());
            assertEquals(Severity.INFO, dimensionRule.getSeverity());
        }
    }
    
    @Nested
    @DisplayName("HeaderRule Tests")
    class HeaderRuleTests {
        
        @Test
        @DisplayName("should create HeaderRule with string pattern")
        void shouldCreateHeaderRuleWithStringPattern() {
            // Given & When
            TableBlock.HeaderRule headerRule = TableBlock.HeaderRule.builder()
                    .required(true)
                    .pattern("^[A-Z][a-zA-Z\\s]+$")
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertTrue(headerRule.isRequired());
            assertNotNull(headerRule.getPattern());
            assertEquals("^[A-Z][a-zA-Z\\s]+$", headerRule.getPattern().pattern());
            assertEquals(Severity.ERROR, headerRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create HeaderRule with Pattern object")
        void shouldCreateHeaderRuleWithPatternObject() {
            // Given
            Pattern pattern = Pattern.compile("^Header.*");
            
            // When
            TableBlock.HeaderRule headerRule = TableBlock.HeaderRule.builder()
                    .required(false)
                    .pattern(pattern)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertFalse(headerRule.isRequired());
            assertEquals(pattern, headerRule.getPattern());
            assertEquals(Severity.WARN, headerRule.getSeverity());
        }
        
        @Test
        @DisplayName("should require severity for HeaderRule")
        void shouldRequireSeverityForHeaderRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                TableBlock.HeaderRule.builder()
                        .required(true)
                        .pattern("test")
                        .build();
            });
        }
    }
    
    @Nested
    @DisplayName("CaptionRule Tests")
    class CaptionRuleTests {
        
        @Test
        @DisplayName("should create CaptionRule with all attributes")
        void shouldCreateCaptionRuleWithAllAttributes() {
            // Given & When
            TableBlock.CaptionRule captionRule = TableBlock.CaptionRule.builder()
                    .required(true)
                    .pattern("^Table \\d+: .*")
                    .minLength(15)
                    .maxLength(150)
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertTrue(captionRule.isRequired());
            assertNotNull(captionRule.getPattern());
            assertEquals(15, captionRule.getMinLength());
            assertEquals(150, captionRule.getMaxLength());
            assertEquals(Severity.ERROR, captionRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create CaptionRule without pattern")
        void shouldCreateCaptionRuleWithoutPattern() {
            // Given & When
            TableBlock.CaptionRule captionRule = TableBlock.CaptionRule.builder()
                    .required(false)
                    .minLength(5)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertFalse(captionRule.isRequired());
            assertNull(captionRule.getPattern());
            assertEquals(5, captionRule.getMinLength());
            assertEquals(100, captionRule.getMaxLength());
            assertEquals(Severity.WARN, captionRule.getSeverity());
        }
        
        @Test
        @DisplayName("should require severity for CaptionRule")
        void shouldRequireSeverityForCaptionRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                TableBlock.CaptionRule.builder()
                        .required(true)
                        .minLength(10)
                        .maxLength(50)
                        .build();
            });
        }
    }
    
    @Nested
    @DisplayName("FormatRule Tests")
    class FormatRuleTests {
        
        @Test
        @DisplayName("should create FormatRule with style and borders")
        void shouldCreateFormatRuleWithStyleAndBorders() {
            // Given & When
            TableBlock.FormatRule formatRule = TableBlock.FormatRule.builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();
            
            // Then
            assertEquals("grid", formatRule.getStyle());
            assertTrue(formatRule.getBorders());
            assertEquals(Severity.INFO, formatRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create FormatRule with only style")
        void shouldCreateFormatRuleWithOnlyStyle() {
            // Given & When
            TableBlock.FormatRule formatRule = TableBlock.FormatRule.builder()
                    .style("simple")
                    .severity(Severity.WARN)
                    .build();
            
            // Then
            assertEquals("simple", formatRule.getStyle());
            assertNull(formatRule.getBorders());
            assertEquals(Severity.WARN, formatRule.getSeverity());
        }
        
        @Test
        @DisplayName("should create FormatRule with only borders")
        void shouldCreateFormatRuleWithOnlyBorders() {
            // Given & When
            TableBlock.FormatRule formatRule = TableBlock.FormatRule.builder()
                    .borders(false)
                    .severity(Severity.ERROR)
                    .build();
            
            // Then
            assertNull(formatRule.getStyle());
            assertFalse(formatRule.getBorders());
            assertEquals(Severity.ERROR, formatRule.getSeverity());
        }
        
        @Test
        @DisplayName("should require severity for FormatRule")
        void shouldRequireSeverityForFormatRule() {
            // When & Then
            assertThrows(NullPointerException.class, () -> {
                TableBlock.FormatRule.builder()
                        .style("grid")
                        .borders(true)
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
            TableBlock.DimensionRule columns1 = TableBlock.DimensionRule.builder()
                    .min(2)
                    .max(8)
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.DimensionRule columns2 = TableBlock.DimensionRule.builder()
                    .min(2)
                    .max(8)
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.HeaderRule header1 = TableBlock.HeaderRule.builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build();
                    
            TableBlock.HeaderRule header2 = TableBlock.HeaderRule.builder()
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.WARN)
                    .build();
                    
            // When
            TableBlock table1 = TableBlock.builder()
                    .severity(Severity.ERROR)
                    .columns(columns1)
                    .header(header1)
                    .build();
                    
            TableBlock table2 = TableBlock.builder()
                    .severity(Severity.ERROR)
                    .columns(columns2)
                    .header(header2)
                    .build();
                    
            TableBlock table3 = TableBlock.builder()
                    .severity(Severity.WARN)
                    .columns(columns1)
                    .header(header1)
                    .build();
            
            // Then
            assertEquals(table1, table2);
            assertNotEquals(table1, table3);
            assertEquals(table1.hashCode(), table2.hashCode());
            assertNotEquals(table1.hashCode(), table3.hashCode());
        }
        
        @Test
        @DisplayName("should test inner class equals and hashCode")
        void shouldTestInnerClassEqualsAndHashCode() {
            // Given
            TableBlock.DimensionRule dim1 = TableBlock.DimensionRule.builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.DimensionRule dim2 = TableBlock.DimensionRule.builder()
                    .min(5)
                    .max(10)
                    .severity(Severity.ERROR)
                    .build();
                    
            TableBlock.HeaderRule header1 = TableBlock.HeaderRule.builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();
                    
            TableBlock.HeaderRule header2 = TableBlock.HeaderRule.builder()
                    .required(false)
                    .pattern("test")
                    .severity(Severity.INFO)
                    .build();
                    
            TableBlock.CaptionRule caption1 = TableBlock.CaptionRule.builder()
                    .required(true)
                    .pattern("^Table.*")
                    .minLength(10)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();
                    
            TableBlock.CaptionRule caption2 = TableBlock.CaptionRule.builder()
                    .required(true)
                    .pattern("^Table.*")
                    .minLength(10)
                    .maxLength(100)
                    .severity(Severity.WARN)
                    .build();
                    
            TableBlock.FormatRule format1 = TableBlock.FormatRule.builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();
                    
            TableBlock.FormatRule format2 = TableBlock.FormatRule.builder()
                    .style("grid")
                    .borders(true)
                    .severity(Severity.INFO)
                    .build();
            
            // Then
            assertEquals(dim1, dim2);
            assertEquals(dim1.hashCode(), dim2.hashCode());
            
            assertEquals(header1, header2);
            assertEquals(header1.hashCode(), header2.hashCode());
            
            assertEquals(caption1, caption2);
            assertEquals(caption1.hashCode(), caption2.hashCode());
            
            assertEquals(format1, format2);
            assertEquals(format1.hashCode(), format2.hashCode());
        }
    }
}