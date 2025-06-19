package com.example.linter.config.loader;

import com.example.linter.config.*;
import com.example.linter.config.blocks.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigurationLoaderTest {
    
    private ConfigurationLoader loader;
    
    @BeforeEach
    void setUp() {
        loader = new ConfigurationLoader();
    }
    
    @Test
    void testLoadFullConfiguration() throws IOException {
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
        
        LinterConfiguration config = loader.loadConfiguration(
            new ByteArrayInputStream(yaml.getBytes())
        );
        
        assertNotNull(config);
        assertNotNull(config.document());
        assertNotNull(config.document().metadata());
        
        // Check metadata attributes
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
        
        // Check sections
        var sections = config.document().sections();
        assertEquals(1, sections.size());
        
        var introSection = sections.get(0);
        assertEquals("introduction", introSection.name());
        assertEquals(1, introSection.order());
        assertEquals(1, introSection.level());
        assertEquals(1, introSection.min());
        assertEquals(1, introSection.max());
        
        // Check title pattern
        assertNotNull(introSection.title());
        assertEquals("^(Introduction|Einführung)$", introSection.title().pattern());
        
        // Check allowed blocks
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
    void testLoadFromFile(@TempDir Path tempDir) throws IOException {
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
        
        LinterConfiguration config = loader.loadConfiguration(configFile);
        
        assertNotNull(config);
        assertEquals(1, config.document().metadata().attributes().size());
    }
    
    @Test
    void testEmptyConfiguration() {
        String yaml = "";
        
        assertThrows(ConfigurationException.class, () -> 
            loader.loadConfiguration(new ByteArrayInputStream(yaml.getBytes()))
        );
    }
    
    @Test
    void testMissingDocumentSection() {
        String yaml = """
            someOtherKey:
              value: test
            """;
        
        ConfigurationException exception = assertThrows(ConfigurationException.class, () -> 
            loader.loadConfiguration(new ByteArrayInputStream(yaml.getBytes()))
        );
        
        assertTrue(exception.getMessage().contains("Missing required 'document' section"));
    }
    
    @Test
    void testInvalidSeverity() {
        String yaml = """
            document:
              metadata:
                attributes:
                  - name: test
                    severity: invalid
              sections: []
            """;
        
        assertThrows(ConfigurationException.class, () -> 
            loader.loadConfiguration(new ByteArrayInputStream(yaml.getBytes()))
        );
    }
    
    @Test
    void testInvalidBlockType() {
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
        
        assertThrows(ConfigurationException.class, () -> 
            loader.loadConfiguration(new ByteArrayInputStream(yaml.getBytes()))
        );
    }
    
    @Test
    void testNestedSections() {
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
        
        LinterConfiguration config = loader.loadConfiguration(
            new ByteArrayInputStream(yaml.getBytes())
        );
        
        var mainSection = config.document().sections().get(0);
        assertEquals(1, mainSection.subsections().size());
        
        var subSection = mainSection.subsections().get(0);
        assertEquals("sub1", subSection.name());
        assertEquals(2, subSection.level());
        assertEquals(1, subSection.subsections().size());
        
        var subSubSection = subSection.subsections().get(0);
        assertEquals(3, subSubSection.level());
        assertEquals(5, subSubSection.max());
    }
    
    @Test
    void testCompleteSpecificationLoads() throws IOException {
        // This test verifies that our complete specification file loads without errors
        Path specPath = Path.of("docs/linter-config-specification.yaml");
        if (Files.exists(specPath)) {
            LinterConfiguration config = loader.loadConfiguration(specPath);
            
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().metadata());
            assertFalse(config.document().metadata().attributes().isEmpty());
            assertFalse(config.document().sections().isEmpty());
        }
    }
    
    @Nested
    class MetadataAttributesTest {
        
        @Test
        void testLoadTwoMetadataAttributes() {
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
            
            LinterConfiguration config = loader.loadConfiguration(
                new ByteArrayInputStream(yaml.getBytes())
            );
            
            assertNotNull(config);
            assertNotNull(config.document());
            assertNotNull(config.document().metadata());
            
            var attributes = config.document().metadata().attributes();
            assertEquals(2, attributes.size());
            
            // Verify first attribute (title)
            var titleAttr = attributes.get(0);
            assertEquals("title", titleAttr.name());
            assertEquals(1, titleAttr.order());
            assertTrue(titleAttr.required());
            assertEquals(5, titleAttr.minLength());
            assertEquals(100, titleAttr.maxLength());
            assertEquals("^[A-Z].*", titleAttr.pattern());
            assertEquals(Severity.ERROR, titleAttr.severity());
            
            // Verify second attribute (author)
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