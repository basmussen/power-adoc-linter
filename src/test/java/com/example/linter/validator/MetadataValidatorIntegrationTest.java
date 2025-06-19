package com.example.linter.validator;

import com.example.linter.config.MetadataConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.AttributeRule;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MetadataValidator Integration Test")
class MetadataValidatorIntegrationTest {

    private Asciidoctor asciidoctor;
    private MetadataValidator validator;
    private Path tempDir;

    @BeforeEach
    void setUp() throws IOException {
        asciidoctor = Asciidoctor.Factory.create();
        tempDir = Files.createTempDirectory("asciidoc-test");
        
        MetadataConfiguration config = MetadataConfiguration.builder()
            .attributes(Arrays.asList(
                AttributeRule.builder()
                    .name("title")
                    .required(true)
                    .minLength(5)
                    .maxLength(100)
                    .pattern("^[A-Z].*")
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("author")
                    .order(2)
                    .required(true)
                    .minLength(5)
                    .maxLength(50)
                    .pattern("^[A-Z][a-zA-Z\\s\\.]+$")
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("revdate")
                    .order(3)
                    .required(true)
                    .pattern("^\\d{4}-\\d{2}-\\d{2}$")
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("version")
                    .order(4)
                    .required(true)
                    .pattern("^\\d+\\.\\d+(\\.\\d+)?$")
                    .severity(Severity.ERROR)
                    .build(),
                AttributeRule.builder()
                    .name("email")
                    .required(false)
                    .pattern("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                    .severity(Severity.WARN)
                    .build()
            ))
            .build();
        
        validator = MetadataValidator.fromConfiguration(config).build();
    }

    @Test
    @DisplayName("should validate valid document without errors")
    void shouldPassValidationWhenDocumentHasAllValidMetadata() throws IOException {
        // Given
        String content = """
            = Valid Document Title
            :author: John Doe
            :revdate: 2024-01-15
            :version: 1.0.0
            :email: john.doe@example.com
            
            == Introduction
            This is a valid document.
            """;
        File docFile = createTempFile("valid-doc.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertFalse(result.hasErrors());
        assertFalse(result.hasWarnings());
        assertEquals(0, result.getMessages().size());
    }

    @Test
    @DisplayName("should detect missing required attributes")
    void shouldReportErrorsWhenRequiredAttributesAreMissing() throws IOException {
        // Given
        String content = """
            = Valid Title
            :email: test@example.com
            
            Content without required metadata.
            """;
        File docFile = createTempFile("missing-attrs.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        assertEquals(3, result.getErrorCount()); // missing author, revdate, version
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("Missing required attribute 'author'")));
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("Missing required attribute 'revdate'")));
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("Missing required attribute 'version'")));
    }

    @Test
    @DisplayName("should detect invalid patterns")
    void shouldReportViolationsWhenAttributesDontMatchPatterns() throws IOException {
        // Given
        String content = """
            = invalid title
            :author: john
            :revdate: 15.01.2024
            :version: 1.0-SNAPSHOT
            :email: invalid-email
            
            Content with invalid metadata patterns.
            """;
        File docFile = createTempFile("invalid-patterns.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        assertTrue(result.hasWarnings());
        // Title pattern error
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'title' does not match required pattern")));
        // Author pattern and length errors
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'author'")));
        // Date format error
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'revdate' does not match required pattern")));
        // Version format error
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'version' does not match required pattern")));
        // Email warning
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'email' does not match required pattern") &&
                       msg.getSeverity() == Severity.WARN));
    }

    @Test
    @DisplayName("should detect length violations")
    void shouldReportErrorsWhenAttributesViolateLengthConstraints() throws IOException {
        // Given
        String content = """
            = Doc
            :author: Jo
            :revdate: 2024-01-15
            :version: 1.0.0
            
            Short title and author.
            """;
        File docFile = createTempFile("length-violations.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        // Title too short
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'title' is too short")));
        // Author too short
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("'author' is too short")));
    }

    @Test
    @DisplayName("should detect order violations")
    void shouldReportErrorsWhenAttributesAreInWrongOrder() throws IOException {
        // Given
        String content = """
            = Valid Document Title
            :version: 1.0.0
            :revdate: 2024-01-15
            :author: John Doe
            
            Attributes in wrong order.
            """;
        File docFile = createTempFile("order-violations.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        assertTrue(result.getMessages().stream()
            .anyMatch(msg -> msg.getMessage().contains("should appear before")));
    }

    @Test
    @DisplayName("should handle empty document")
    void shouldReportAllMissingAttributesWhenDocumentIsEmpty() throws IOException {
        // Given
        String content = "";
        File docFile = createTempFile("empty.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        
        // When
        ValidationResult result = validator.validate(document);
        
        // Then
        assertTrue(result.hasErrors());
        assertEquals(4, result.getErrorCount()); // All required attributes missing
    }

    @Test
    @DisplayName("should generate readable validation report")
    void shouldGenerateReadableReportWhenPrintingValidationResult() throws IOException {
        // Given
        String content = """
            = test
            :author: j
            
            Invalid document.
            """;
        File docFile = createTempFile("report-test.adoc", content);
        Document document = asciidoctor.loadFile(docFile, org.asciidoctor.Options.builder().build());
        ValidationResult result = validator.validate(document);
        
        // When
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream ps = new java.io.PrintStream(baos);
        java.io.PrintStream old = System.out;
        System.setOut(ps);
        
        result.printReport();
        
        System.out.flush();
        System.setOut(old);
        
        // Then
        String report = baos.toString();
        assertTrue(report.contains("Validation Report"));
        assertTrue(report.contains("[ERROR]"));
        assertTrue(report.contains("errors"));
        assertTrue(report.contains("warnings"));
    }

    private File createTempFile(String filename, String content) throws IOException {
        Path filePath = tempDir.resolve(filename);
        Files.writeString(filePath, content);
        return filePath.toFile();
    }
}