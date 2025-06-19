package com.example.linter.validator;

import com.example.linter.config.MetadataConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.AttributeRule;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetadataValidator Unit Test")
class MetadataValidatorTest {

    private MetadataConfiguration testConfig;

    @BeforeEach
    void setUp() {
        testConfig = MetadataConfiguration.builder()
            .attributes(Arrays.asList(
                AttributeRule.builder()
                    .name("title")
                    .required(true)
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("author")
                    .required(true)
                    .severity(Severity.ERROR)
                    .build()
            ))
            .build();
    }

    @Test
    @DisplayName("should build validator from configuration")
    void shouldBuildValidatorFromConfiguration() {
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        assertNotNull(validator);
    }

    @Test
    @DisplayName("should validate with mock document")
    void shouldValidateWithMockDocument() {
        // Create a simple test document using AsciidoctorJ
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        
        String content = """
            = Test Document
            :author: Test Author
            
            Content here.
            """;
        
        Document document = asciidoctor.load(content, org.asciidoctor.Options.builder().build());
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        
        ValidationResult result = validator.validate(document);
        
        assertNotNull(result);
        assertFalse(result.hasErrors());
    }

    @Test
    @DisplayName("should detect missing title")
    void shouldDetectMissingTitle() throws IOException {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        
        String content = """
            :author: Test Author
            
            Document without title.
            """;
        
        Path tempFile = Files.createTempFile("test", ".adoc");
        Files.writeString(tempFile, content);
        
        Document document = asciidoctor.loadFile(tempFile.toFile(), org.asciidoctor.Options.builder().build());
        MetadataValidator validator = MetadataValidator.fromConfiguration(testConfig).build();
        
        ValidationResult result = validator.validate(document);
        
        assertTrue(result.hasErrors());
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("Missing required attribute 'title'")));
        
        Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("should properly extract line numbers")
    void shouldProperlyExtractLineNumbers() {
        Asciidoctor asciidoctor = Asciidoctor.Factory.create();
        
        String content = """
            = Test Title
            :author: John Doe
            :version: 1.0
            
            Content.
            """;
        
        Document document = asciidoctor.load(content, org.asciidoctor.Options.builder().build());
        
        MetadataConfiguration config = MetadataConfiguration.builder()
            .attributes(Arrays.asList(
                AttributeRule.builder()
                    .name("author")
                    .order(1)
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("version")
                    .order(2)
                    .severity(Severity.ERROR)
                    .build()
            ))
            .build();
        
        MetadataValidator validator = MetadataValidator.fromConfiguration(config).build();
        ValidationResult result = validator.validate(document);
        
        assertNotNull(result);
        // Check that messages have proper line information
        result.getMessages().forEach(msg -> {
            assertNotNull(msg.getLocation());
            assertTrue(msg.getLocation().getStartLine() > 0);
        });
    }
}