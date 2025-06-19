package com.example.linter.config.loader;

import com.example.linter.config.*;
import com.example.linter.config.blocks.*;
import com.example.linter.rule.AttributeRule;
import com.example.linter.rule.SectionRule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConfigurationLoader")
class ConfigurationLoaderTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader();
    }
    
    @Nested
    @DisplayName("Common Loading Scenarios")
    class CommonTest {
        
        @Test
        @DisplayName("should load full configuration with metadata and sections")
        void shouldLoadFullConfigurationWithMetadataAndSections() throws IOException {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        order: 1
                        required: true
                        minLength: 5
                        maxLength: 100
                        pattern: "^[A-Z].*"
                        severity: error
                      - name: author
                        required: false
                        severity: warn
                  sections:
                    - name: introduction
                      order: 1
                      level: 1
                      min: 1
                      max: 1
                      title:
                        pattern: "^(Introduction|Einführung)$"
                      allowedBlocks:
                        - paragraph:
                            name: intro-paragraph
                            severity: warn
                            occurrence:
                              min: 1
                              max: 3
                              severity: error
                            lines:
                              max: 15
                        - image:
                            severity: info
                            occurrence:
                              min: 0
                              max: 1
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Configuration structure
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().metadata());
            
            // Then - Metadata attributes
            var attributes = config.document().metadata().attributes();
            assertEquals(2, attributes.size());
            
            var titleAttr = attributes.get(0);
            assertEquals("title", titleAttr.name());
            assertEquals(1, titleAttr.order());
            assertTrue(titleAttr.required());
            assertEquals(5, titleAttr.minLength());
            assertEquals(100, titleAttr.maxLength());
            assertEquals("^[A-Z].*", titleAttr.pattern());
            assertEquals(Severity.ERROR, titleAttr.severity());
            
            // Then - Sections
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var introSection = sections.get(0);
            assertEquals("introduction", introSection.name());
            assertEquals(1, introSection.order());
            assertEquals(1, introSection.level());
            assertEquals(1, introSection.min());
            assertEquals(1, introSection.max());
            
            // Then - Title pattern
            assertNotNull(introSection.title());
            assertEquals("^(Introduction|Einführung)$", introSection.title().pattern());
            
            // Then - Allowed blocks
            var allowedBlocks = introSection.allowedBlocks();
            assertEquals(2, allowedBlocks.size());
            
            var abstractBlock = allowedBlocks.get(0);
            assertTrue(abstractBlock instanceof ParagraphBlock);
            ParagraphBlock paragraphBlock = (ParagraphBlock) abstractBlock;
            assertEquals(BlockType.PARAGRAPH, paragraphBlock.getType());
            assertEquals("intro-paragraph", paragraphBlock.getName());
            assertEquals(Severity.WARN, paragraphBlock.getSeverity());
            assertNotNull(paragraphBlock.getOccurrence());
            assertEquals(1, paragraphBlock.getOccurrence().min());
            assertEquals(3, paragraphBlock.getOccurrence().max());
            assertEquals(Severity.ERROR, paragraphBlock.getOccurrence().severity());
            assertNotNull(paragraphBlock.getLines());
            assertEquals(15, paragraphBlock.getLines().max());
        }
        
        @Test
        @DisplayName("should load configuration from file")
        void shouldLoadConfigurationFromFile(@TempDir Path tempDir) throws IOException {
            // Given
            Path configFile = tempDir.resolve("test-config.yaml");
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: version
                        required: true
                        pattern: "^\\\\d+\\\\.\\\\d+$"
                        severity: error
                  sections: []
                """;
            Files.writeString(configFile, yaml);
            
            // When
            LinterConfiguration config = loader.loadConfiguration(configFile);
            
            // Then
            assertNotNull(config);
            assertEquals(1, config.document().metadata().attributes().size());
        }
        
        @Test
        @DisplayName("should throw exception when configuration is empty")
        void shouldThrowExceptionWhenConfigurationIsEmpty() {
            // Given
            String yaml = "";
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> 
                loader.loadConfiguration(yaml)
            );
        }
        
        @Test
        @DisplayName("should throw exception when document section is missing")
        void shouldThrowExceptionWhenDocumentSectionIsMissing() {
            // Given
            String yaml = """
                someOtherKey:
                  value: test
                """;
            
            // When
            ConfigurationException exception = assertThrows(ConfigurationException.class, () -> 
                loader.loadConfiguration(yaml)
            );
            
            // Then
            assertTrue(exception.getMessage().contains("Missing required 'document' section"));
        }
        
        @Test
        @DisplayName("should throw exception when severity is invalid")
        void shouldThrowExceptionWhenSeverityIsInvalid() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: test
                        severity: invalid
                  sections: []
                """;
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> 
                loader.loadConfiguration(yaml)
            );
        }
        
        @Test
        @DisplayName("should throw exception when block type is invalid")
        void shouldThrowExceptionWhenBlockTypeIsInvalid() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - level: 1
                      allowedBlocks:
                        - unknownblock:
                            severity: error
                """;
            
            // When & Then
            assertThrows(ConfigurationException.class, () -> 
                loader.loadConfiguration(yaml)
            );
        }
        
        @Test
        @DisplayName("should load nested sections with multiple levels")
        void shouldLoadNestedSectionsWithMultipleLevels() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: main
                      level: 1
                      min: 1
                      max: 1
                      subsections:
                        - name: sub1
                          level: 2
                          min: 0
                          max: 2
                          subsections:
                            - level: 3
                              min: 0
                              max: 5
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Main section
            var mainSection = config.document().sections().get(0);
            assertEquals(1, mainSection.subsections().size());
            
            // Then - Sub section
            var subSection = mainSection.subsections().get(0);
            assertEquals("sub1", subSection.name());
            assertEquals(2, subSection.level());
            assertEquals(1, subSection.subsections().size());
            
            // Then - Sub-sub section
            var subSubSection = subSection.subsections().get(0);
            assertEquals(3, subSubSection.level());
            assertEquals(5, subSubSection.max());
        }
        
        @Test
        @DisplayName("should load complete specification file without errors")
        void shouldLoadCompleteSpecificationFileWithoutErrors() throws IOException {
            // Given
            Path specPath = Path.of("docs/linter-config-specification.yaml");
            
            // When
            if (Files.exists(specPath)) {
                LinterConfiguration config = loader.loadConfiguration(specPath);
                
                // Then
                assertNotNull(config);
                assertNotNull(config.document());
                assertNotNull(config.document().metadata());
                assertFalse(config.document().metadata().attributes().isEmpty());
                assertFalse(config.document().sections().isEmpty());
            }
        }
    }
    
    @Nested
    @DisplayName("Sections Loading")
    class SectionsTest {
        
        @Test
        @DisplayName("should load two sections with all attributes and subsections")
        void shouldLoadTwoSectionsWithAllAttributesAndSubsections() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: introduction
                      order: 1
                      level: 1
                      min: 1
                      max: 1
                      title:
                        pattern: "^(Introduction|Einführung)$"
                      subsections:
                        - name: motivation
                          order: 1
                          level: 2
                          min: 0
                          max: 1
                          title:
                            pattern: "^Motivation$"
                        - name: goals
                          order: 2
                          level: 2
                          min: 0
                          max: 1
                          title:
                            pattern: "^(Goals|Ziele)$"
                    - name: mainContent
                      order: 2
                      level: 1
                      min: 1
                      max: 5
                      title:
                        pattern: "^Chapter.*"
                      subsections:
                        - name: details
                          order: 1
                          level: 2
                          min: 1
                          max: 3
                          title:
                            pattern: "^Details.*"
                        - name: examples
                          order: 2
                          level: 2
                          min: 0
                          max: 10
                          title:
                            pattern: "^Example.*"
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Basic structure
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().sections());
            
            var sections = config.document().sections();
            assertEquals(2, sections.size());
            
            // Then - First section (introduction)
            var introSection = sections.get(0);
            assertEquals("introduction", introSection.name());
            assertEquals(1, introSection.order());
            assertEquals(1, introSection.level());
            assertEquals(1, introSection.min());
            assertEquals(1, introSection.max());
            assertNotNull(introSection.title());
            assertEquals("^(Introduction|Einführung)$", introSection.title().pattern());
            
            // Then - Introduction subsections
            var introSubsections = introSection.subsections();
            assertEquals(2, introSubsections.size());
            
            var motivationSection = introSubsections.get(0);
            assertEquals("motivation", motivationSection.name());
            assertEquals(1, motivationSection.order());
            assertEquals(2, motivationSection.level());
            assertEquals(0, motivationSection.min());
            assertEquals(1, motivationSection.max());
            assertEquals("^Motivation$", motivationSection.title().pattern());
            
            var goalsSection = introSubsections.get(1);
            assertEquals("goals", goalsSection.name());
            assertEquals(2, goalsSection.order());
            assertEquals(2, goalsSection.level());
            assertEquals(0, goalsSection.min());
            assertEquals(1, goalsSection.max());
            assertEquals("^(Goals|Ziele)$", goalsSection.title().pattern());
            
            // Then - Second section (mainContent)
            var mainSection = sections.get(1);
            assertEquals("mainContent", mainSection.name());
            assertEquals(2, mainSection.order());
            assertEquals(1, mainSection.level());
            assertEquals(1, mainSection.min());
            assertEquals(5, mainSection.max());
            assertNotNull(mainSection.title());
            assertEquals("^Chapter.*", mainSection.title().pattern());
            
            // Then - Main content subsections
            var mainSubsections = mainSection.subsections();
            assertEquals(2, mainSubsections.size());
            
            var detailsSection = mainSubsections.get(0);
            assertEquals("details", detailsSection.name());
            assertEquals(1, detailsSection.order());
            assertEquals(2, detailsSection.level());
            assertEquals(1, detailsSection.min());
            assertEquals(3, detailsSection.max());
            assertEquals("^Details.*", detailsSection.title().pattern());
            
            var examplesSection = mainSubsections.get(1);
            assertEquals("examples", examplesSection.name());
            assertEquals(2, examplesSection.order());
            assertEquals(2, examplesSection.level());
            assertEquals(0, examplesSection.min());
            assertEquals(10, examplesSection.max());
            assertEquals("^Example.*", examplesSection.title().pattern());
        }
    }
    
    @Nested
    @DisplayName("Metadata Attributes Loading")
    class MetadataAttributesTest {
        
        @Test
        @DisplayName("should load two metadata attributes with all properties")
        void shouldLoadTwoMetadataAttributesWithAllProperties() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes:
                      - name: title
                        order: 1
                        required: true
                        minLength: 5
                        maxLength: 100
                        pattern: "^[A-Z].*"
                        severity: error
                      - name: author
                        order: 2
                        required: false
                        minLength: 3
                        maxLength: 50
                        pattern: "^[A-Za-z][a-zA-Z\\\\s\\\\.]+$"
                        severity: warn
                  sections: []
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Basic structure
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().metadata());
            
            var attributes = config.document().metadata().attributes();
            assertEquals(2, attributes.size());
            
            // Then - First attribute (title)
            var titleAttr = attributes.get(0);
            assertEquals("title", titleAttr.name());
            assertEquals(1, titleAttr.order());
            assertTrue(titleAttr.required());
            assertEquals(5, titleAttr.minLength());
            assertEquals(100, titleAttr.maxLength());
            assertEquals("^[A-Z].*", titleAttr.pattern());
            assertEquals(Severity.ERROR, titleAttr.severity());
            
            // Then - Second attribute (author)
            var authorAttr = attributes.get(1);
            assertEquals("author", authorAttr.name());
            assertEquals(2, authorAttr.order());
            assertFalse(authorAttr.required());
            assertEquals(3, authorAttr.minLength());
            assertEquals(50, authorAttr.maxLength());
            assertEquals("^[A-Za-z][a-zA-Z\\s\\.]+$", authorAttr.pattern());
            assertEquals(Severity.WARN, authorAttr.severity());
        }
    }
}