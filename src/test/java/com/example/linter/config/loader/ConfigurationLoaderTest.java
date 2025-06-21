package com.example.linter.config.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.linter.config.BlockType;
import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.ImageBlock;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.blocks.TableBlock;
import com.example.linter.config.blocks.VerseBlock;

@DisplayName("ConfigurationLoader")
class ConfigurationLoaderTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader(true); // Skip schema validation for existing tests
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
            // Jackson will throw a different error, so we check for ConfigurationException
            assertNotNull(exception);
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
    
    @Nested
    @DisplayName("ImageBlock Loading")
    class ImageBlockTest {
        
        @Test
        @DisplayName("should load ImageBlock in section and subsection")
        void shouldLoadImageBlockInSectionAndSubsection() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: main-section
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - image:
                            name: section-image
                            severity: warn
                            occurrence:
                              min: 0
                              max: 3
                            url:
                              required: true
                              pattern: "^https?://.*\\\\.(jpg|jpeg|png|gif|svg)$"
                            height:
                              required: false
                              minValue: 100
                              maxValue: 2000
                            width:
                              required: false
                              minValue: 100
                              maxValue: 3000
                            alt:
                              required: true
                              minLength: 10
                              maxLength: 200
                      subsections:
                        - name: sub-section
                          level: 2
                          min: 1
                          max: 2
                          allowedBlocks:
                            - image:
                                name: subsection-image
                                severity: info
                                occurrence:
                                  min: 1
                                  max: 5
                                url:
                                  required: true
                                  pattern: ".*\\\\.(jpg|png)$"
                                alt:
                                  required: true
                                  minLength: 5
                                  maxLength: 100
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Section level image block
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var mainSection = sections.get(0);
            assertEquals("main-section", mainSection.name());
            assertEquals(1, mainSection.allowedBlocks().size());
            
            var sectionImage = (ImageBlock) mainSection.allowedBlocks().get(0);
            assertEquals("section-image", sectionImage.getName());
            assertEquals(Severity.WARN, sectionImage.getSeverity());
            assertEquals(0, sectionImage.getOccurrence().min());
            assertEquals(3, sectionImage.getOccurrence().max());
            
            // URL validation
            assertNotNull(sectionImage.getUrl());
            assertTrue(sectionImage.getUrl().isRequired());
            assertEquals("^https?://.*\\.(jpg|jpeg|png|gif|svg)$", sectionImage.getUrl().getPattern().pattern());
            
            // Dimension validation
            assertNotNull(sectionImage.getHeight());
            assertFalse(sectionImage.getHeight().isRequired());
            assertEquals(100, sectionImage.getHeight().getMinValue());
            assertEquals(2000, sectionImage.getHeight().getMaxValue());
            
            assertNotNull(sectionImage.getWidth());
            assertFalse(sectionImage.getWidth().isRequired());
            assertEquals(100, sectionImage.getWidth().getMinValue());
            assertEquals(3000, sectionImage.getWidth().getMaxValue());
            
            // Alt text validation
            assertNotNull(sectionImage.getAlt());
            assertTrue(sectionImage.getAlt().isRequired());
            assertEquals(10, sectionImage.getAlt().getMinLength());
            assertEquals(200, sectionImage.getAlt().getMaxLength());
            
            // Then - Subsection level image block
            var subsections = mainSection.subsections();
            assertEquals(1, subsections.size());
            
            var subSection = subsections.get(0);
            assertEquals("sub-section", subSection.name());
            assertEquals(1, subSection.allowedBlocks().size());
            
            var subsectionImage = (ImageBlock) subSection.allowedBlocks().get(0);
            assertEquals("subsection-image", subsectionImage.getName());
            assertEquals(Severity.INFO, subsectionImage.getSeverity());
            assertEquals(1, subsectionImage.getOccurrence().min());
            assertEquals(5, subsectionImage.getOccurrence().max());
            
            // Subsection image validation rules
            assertTrue(subsectionImage.getUrl().isRequired());
            assertEquals(".*\\.(jpg|png)$", subsectionImage.getUrl().getPattern().pattern());
            assertTrue(subsectionImage.getAlt().isRequired());
            assertEquals(5, subsectionImage.getAlt().getMinLength());
            assertEquals(100, subsectionImage.getAlt().getMaxLength());
        }
    }
    
    @Nested
    @DisplayName("ListingBlock Loading")
    class ListingBlockTest {
        
        @Test
        @DisplayName("should load ListingBlock in section and subsection")
        void shouldLoadListingBlockInSectionAndSubsection() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: code-section
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - listing:
                            name: main-code
                            severity: error
                            occurrence:
                              min: 1
                              max: 10
                            language:
                              required: true
                              allowed: ["java", "python", "javascript"]
                              severity: error
                            lines:
                              min: 5
                              max: 200
                            title:
                              required: true
                              pattern: "^Listing \\\\d+:.*"
                              severity: warn
                            callouts:
                              allowed: true
                              max: 15
                              severity: info
                      subsections:
                        - name: examples
                          level: 2
                          min: 1
                          max: 3
                          allowedBlocks:
                            - listing:
                                name: example-code
                                severity: warn
                                occurrence:
                                  min: 1
                                  max: 5
                                language:
                                  required: false
                                  allowed: ["yaml", "json", "xml"]
                                  severity: warn
                                lines:
                                  min: 1
                                  max: 50
                                title:
                                  required: false
                                  pattern: "^Example.*"
                                  severity: info
                                callouts:
                                  allowed: false
                                  severity: error
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Section level listing block
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var codeSection = sections.get(0);
            assertEquals("code-section", codeSection.name());
            assertEquals(1, codeSection.allowedBlocks().size());
            
            var mainListing = (ListingBlock) codeSection.allowedBlocks().get(0);
            assertEquals("main-code", mainListing.getName());
            assertEquals(Severity.ERROR, mainListing.getSeverity());
            assertEquals(1, mainListing.getOccurrence().min());
            assertEquals(10, mainListing.getOccurrence().max());
            
            // Language validation
            assertNotNull(mainListing.getLanguage());
            assertTrue(mainListing.getLanguage().isRequired());
            assertEquals(3, mainListing.getLanguage().getAllowed().size());
            assertTrue(mainListing.getLanguage().getAllowed().contains("java"));
            assertTrue(mainListing.getLanguage().getAllowed().contains("python"));
            assertTrue(mainListing.getLanguage().getAllowed().contains("javascript"));
            assertEquals(Severity.ERROR, mainListing.getLanguage().getSeverity());
            
            // Lines validation
            assertNotNull(mainListing.getLines());
            assertEquals(5, mainListing.getLines().min());
            assertEquals(200, mainListing.getLines().max());
            
            // Title validation
            assertNotNull(mainListing.getTitle());
            assertTrue(mainListing.getTitle().isRequired());
            assertEquals("^Listing \\d+:.*", mainListing.getTitle().getPattern().pattern());
            assertEquals(Severity.WARN, mainListing.getTitle().getSeverity());
            
            // Callouts validation
            assertNotNull(mainListing.getCallouts());
            assertTrue(mainListing.getCallouts().isAllowed());
            assertEquals(15, mainListing.getCallouts().getMax());
            assertEquals(Severity.INFO, mainListing.getCallouts().getSeverity());
            
            // Then - Subsection level listing block
            var subsections = codeSection.subsections();
            assertEquals(1, subsections.size());
            
            var examplesSection = subsections.get(0);
            assertEquals("examples", examplesSection.name());
            assertEquals(1, examplesSection.allowedBlocks().size());
            
            var exampleListing = (ListingBlock) examplesSection.allowedBlocks().get(0);
            assertEquals("example-code", exampleListing.getName());
            assertEquals(Severity.WARN, exampleListing.getSeverity());
            
            // Subsection listing validation rules
            assertFalse(exampleListing.getLanguage().isRequired());
            assertEquals(3, exampleListing.getLanguage().getAllowed().size());
            assertTrue(exampleListing.getLanguage().getAllowed().contains("yaml"));
            assertEquals(1, exampleListing.getLines().min());
            assertEquals(50, exampleListing.getLines().max());
            assertFalse(exampleListing.getTitle().isRequired());
            assertFalse(exampleListing.getCallouts().isAllowed());
        }
    }
    
    @Nested
    @DisplayName("ParagraphBlock Loading")
    class ParagraphBlockTest {
        
        @Test
        @DisplayName("should load ParagraphBlock in section and subsection")
        void shouldLoadParagraphBlockInSectionAndSubsection() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: intro-section
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - paragraph:
                            name: intro-paragraph
                            severity: warn
                            occurrence:
                              min: 1
                              max: 5
                              severity: error
                            lines:
                              min: 3
                              max: 20
                      subsections:
                        - name: details
                          level: 2
                          min: 0
                          max: 3
                          allowedBlocks:
                            - paragraph:
                                name: detail-paragraph
                                severity: info
                                occurrence:
                                  min: 1
                                  max: 10
                                lines:
                                  min: 1
                                  max: 15
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Section level paragraph block
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var introSection = sections.get(0);
            assertEquals("intro-section", introSection.name());
            assertEquals(1, introSection.allowedBlocks().size());
            
            var introParagraph = (ParagraphBlock) introSection.allowedBlocks().get(0);
            assertEquals("intro-paragraph", introParagraph.getName());
            assertEquals(Severity.WARN, introParagraph.getSeverity());
            assertEquals(1, introParagraph.getOccurrence().min());
            assertEquals(5, introParagraph.getOccurrence().max());
            assertEquals(Severity.ERROR, introParagraph.getOccurrence().severity());
            
            // Lines validation
            assertNotNull(introParagraph.getLines());
            assertEquals(3, introParagraph.getLines().min());
            assertEquals(20, introParagraph.getLines().max());
            
            // Then - Subsection level paragraph block
            var subsections = introSection.subsections();
            assertEquals(1, subsections.size());
            
            var detailsSection = subsections.get(0);
            assertEquals("details", detailsSection.name());
            assertEquals(1, detailsSection.allowedBlocks().size());
            
            var detailParagraph = (ParagraphBlock) detailsSection.allowedBlocks().get(0);
            assertEquals("detail-paragraph", detailParagraph.getName());
            assertEquals(Severity.INFO, detailParagraph.getSeverity());
            assertEquals(1, detailParagraph.getOccurrence().min());
            assertEquals(10, detailParagraph.getOccurrence().max());
            
            // Subsection paragraph lines validation
            assertEquals(1, detailParagraph.getLines().min());
            assertEquals(15, detailParagraph.getLines().max());
        }
    }
    
    @Nested
    @DisplayName("TableBlock Loading")
    class TableBlockTest {
        
        @Test
        @DisplayName("should load TableBlock in section and subsection")
        void shouldLoadTableBlockInSectionAndSubsection() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: data-section
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - table:
                            name: main-table
                            severity: error
                            occurrence:
                              min: 1
                              max: 3
                            columns:
                              min: 2
                              max: 10
                              severity: error
                            rows:
                              min: 1
                              max: 100
                              severity: warn
                            header:
                              required: true
                              pattern: "^[A-Z].*"
                              severity: error
                            caption:
                              required: true
                              pattern: "^Table \\\\d+:.*"
                              minLength: 10
                              maxLength: 200
                              severity: warn
                            format:
                              style: "grid"
                              borders: true
                              severity: info
                      subsections:
                        - name: summary
                          level: 2
                          min: 0
                          max: 2
                          allowedBlocks:
                            - table:
                                name: summary-table
                                severity: warn
                                occurrence:
                                  min: 0
                                  max: 5
                                columns:
                                  min: 2
                                  max: 5
                                  severity: warn
                                rows:
                                  min: 1
                                  max: 50
                                  severity: info
                                header:
                                  required: false
                                  pattern: ".*"
                                  severity: info
                                caption:
                                  required: false
                                  minLength: 5
                                  maxLength: 100
                                  severity: info
                                format:
                                  style: "simple"
                                  borders: false
                                  severity: info
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Section level table block
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var dataSection = sections.get(0);
            assertEquals("data-section", dataSection.name());
            assertEquals(1, dataSection.allowedBlocks().size());
            
            var mainTable = (TableBlock) dataSection.allowedBlocks().get(0);
            assertEquals("main-table", mainTable.getName());
            assertEquals(Severity.ERROR, mainTable.getSeverity());
            assertEquals(1, mainTable.getOccurrence().min());
            assertEquals(3, mainTable.getOccurrence().max());
            
            // Columns validation
            assertNotNull(mainTable.getColumns());
            assertEquals(2, mainTable.getColumns().getMin());
            assertEquals(10, mainTable.getColumns().getMax());
            assertEquals(Severity.ERROR, mainTable.getColumns().getSeverity());
            
            // Rows validation
            assertNotNull(mainTable.getRows());
            assertEquals(1, mainTable.getRows().getMin());
            assertEquals(100, mainTable.getRows().getMax());
            assertEquals(Severity.WARN, mainTable.getRows().getSeverity());
            
            // Header validation
            assertNotNull(mainTable.getHeader());
            assertTrue(mainTable.getHeader().isRequired());
            assertEquals("^[A-Z].*", mainTable.getHeader().getPattern().pattern());
            assertEquals(Severity.ERROR, mainTable.getHeader().getSeverity());
            
            // Caption validation
            assertNotNull(mainTable.getCaption());
            assertTrue(mainTable.getCaption().isRequired());
            assertEquals("^Table \\d+:.*", mainTable.getCaption().getPattern().pattern());
            assertEquals(10, mainTable.getCaption().getMinLength());
            assertEquals(200, mainTable.getCaption().getMaxLength());
            assertEquals(Severity.WARN, mainTable.getCaption().getSeverity());
            
            // Format validation
            assertNotNull(mainTable.getFormat());
            assertEquals("grid", mainTable.getFormat().getStyle());
            assertTrue(mainTable.getFormat().getBorders());
            assertEquals(Severity.INFO, mainTable.getFormat().getSeverity());
            
            // Then - Subsection level table block
            var subsections = dataSection.subsections();
            assertEquals(1, subsections.size());
            
            var summarySection = subsections.get(0);
            assertEquals("summary", summarySection.name());
            assertEquals(1, summarySection.allowedBlocks().size());
            
            var summaryTable = (TableBlock) summarySection.allowedBlocks().get(0);
            assertEquals("summary-table", summaryTable.getName());
            assertEquals(Severity.WARN, summaryTable.getSeverity());
            
            // Subsection table validation rules
            assertEquals(2, summaryTable.getColumns().getMin());
            assertEquals(5, summaryTable.getColumns().getMax());
            assertEquals(50, summaryTable.getRows().getMax());
            assertFalse(summaryTable.getHeader().isRequired());
            assertFalse(summaryTable.getCaption().isRequired());
            assertEquals("simple", summaryTable.getFormat().getStyle());
            assertFalse(summaryTable.getFormat().getBorders());
        }
    }
    
    @Nested
    @DisplayName("VerseBlock Loading")
    class VerseBlockTest {
        
        @Test
        @DisplayName("should load VerseBlock in section and subsection")
        void shouldLoadVerseBlockInSectionAndSubsection() {
            // Given
            String yaml = """
                document:
                  metadata:
                    attributes: []
                  sections:
                    - name: quotes-section
                      level: 1
                      min: 1
                      max: 1
                      allowedBlocks:
                        - verse:
                            name: main-verse
                            severity: warn
                            occurrence:
                              min: 0
                              max: 3
                            author:
                              defaultValue: "Unknown"
                              minLength: 3
                              maxLength: 50
                              pattern: "^[A-Z][a-zA-Z\\\\s\\\\.]+$"
                              required: true
                            attribution:
                              defaultValue: "Source Unknown"
                              minLength: 5
                              maxLength: 100
                              pattern: "^[A-Za-z0-9\\\\s,\\\\.]+$"
                              required: false
                            content:
                              minLength: 20
                              maxLength: 500
                              pattern: ".*\\\\n.*"
                              required: true
                      subsections:
                        - name: poetry
                          level: 2
                          min: 0
                          max: 5
                          allowedBlocks:
                            - verse:
                                name: poetry-verse
                                severity: info
                                occurrence:
                                  min: 1
                                  max: 10
                                author:
                                  minLength: 2
                                  maxLength: 40
                                  required: false
                                attribution:
                                  minLength: 3
                                  maxLength: 80
                                  required: false
                                content:
                                  minLength: 10
                                  maxLength: 300
                                  required: true
                """;
            
            // When
            LinterConfiguration config = loader.loadConfiguration(yaml);
            
            // Then - Section level verse block
            var sections = config.document().sections();
            assertEquals(1, sections.size());
            
            var quotesSection = sections.get(0);
            assertEquals("quotes-section", quotesSection.name());
            assertEquals(1, quotesSection.allowedBlocks().size());
            
            var mainVerse = (VerseBlock) quotesSection.allowedBlocks().get(0);
            assertEquals("main-verse", mainVerse.getName());
            assertEquals(Severity.WARN, mainVerse.getSeverity());
            assertEquals(0, mainVerse.getOccurrence().min());
            assertEquals(3, mainVerse.getOccurrence().max());
            
            // Author validation
            assertNotNull(mainVerse.getAuthor());
            assertEquals("Unknown", mainVerse.getAuthor().getDefaultValue());
            assertEquals(3, mainVerse.getAuthor().getMinLength());
            assertEquals(50, mainVerse.getAuthor().getMaxLength());
            assertEquals("^[A-Z][a-zA-Z\\s\\.]+$", mainVerse.getAuthor().getPattern().pattern());
            assertTrue(mainVerse.getAuthor().isRequired());
            
            // Attribution validation
            assertNotNull(mainVerse.getAttribution());
            assertEquals("Source Unknown", mainVerse.getAttribution().getDefaultValue());
            assertEquals(5, mainVerse.getAttribution().getMinLength());
            assertEquals(100, mainVerse.getAttribution().getMaxLength());
            assertEquals("^[A-Za-z0-9\\s,\\.]+$", mainVerse.getAttribution().getPattern().pattern());
            assertFalse(mainVerse.getAttribution().isRequired());
            
            // Content validation
            assertNotNull(mainVerse.getContent());
            assertEquals(20, mainVerse.getContent().getMinLength());
            assertEquals(500, mainVerse.getContent().getMaxLength());
            assertEquals(".*\\n.*", mainVerse.getContent().getPattern().pattern());
            assertTrue(mainVerse.getContent().isRequired());
            
            // Then - Subsection level verse block
            var subsections = quotesSection.subsections();
            assertEquals(1, subsections.size());
            
            var poetrySection = subsections.get(0);
            assertEquals("poetry", poetrySection.name());
            assertEquals(1, poetrySection.allowedBlocks().size());
            
            var poetryVerse = (VerseBlock) poetrySection.allowedBlocks().get(0);
            assertEquals("poetry-verse", poetryVerse.getName());
            assertEquals(Severity.INFO, poetryVerse.getSeverity());
            assertEquals(1, poetryVerse.getOccurrence().min());
            assertEquals(10, poetryVerse.getOccurrence().max());
            
            // Subsection verse validation rules
            assertEquals(2, poetryVerse.getAuthor().getMinLength());
            assertEquals(40, poetryVerse.getAuthor().getMaxLength());
            assertFalse(poetryVerse.getAuthor().isRequired());
            assertEquals(10, poetryVerse.getContent().getMinLength());
            assertEquals(300, poetryVerse.getContent().getMaxLength());
        }
    }
}