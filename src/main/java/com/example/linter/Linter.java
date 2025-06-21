package com.example.linter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.ast.Document;
import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.rule.SectionConfig;
import com.example.linter.validator.BlockValidator;
import com.example.linter.validator.MetadataValidator;
import com.example.linter.validator.SectionValidator;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;

/**
 * Main entry point for the AsciiDoc linter.
 * Provides methods to validate AsciiDoc files against a configuration.
 */
public class Linter {
    
    private final Asciidoctor asciidoctor;
    
    public Linter() {
        this.asciidoctor = Asciidoctor.Factory.create();
    }
    
    /**
     * Validates a single AsciiDoc file.
     * 
     * @param file the file to validate
     * @param config the linter configuration
     * @return validation result
     * @throws IOException if the file cannot be read
     */
    public ValidationResult validateFile(Path file, LinterConfiguration config) throws IOException {
        Objects.requireNonNull(file, "file must not be null");
        Objects.requireNonNull(config, "config must not be null");
        
        if (!Files.exists(file)) {
            throw new IOException("File does not exist: " + file);
        }
        
        if (!Files.isRegularFile(file)) {
            throw new IOException("Not a regular file: " + file);
        }
        
        return performValidation(file, config);
    }
    
    /**
     * Validates multiple AsciiDoc files.
     * 
     * @param files the files to validate
     * @param config the linter configuration
     * @return map of file to validation result
     */
    public Map<Path, ValidationResult> validateFiles(List<Path> files, LinterConfiguration config) {
        Objects.requireNonNull(files, "files must not be null");
        Objects.requireNonNull(config, "config must not be null");
        
        Map<Path, ValidationResult> results = new LinkedHashMap<>();
        
        for (Path file : files) {
            try {
                ValidationResult result = validateFile(file, config);
                results.put(file, result);
            } catch (IOException e) {
                // Create error result
                ValidationResult errorResult = createIOErrorResult(file, e);
                results.put(file, errorResult);
            }
        }
        
        return results;
    }
    
    /**
     * Validates all matching files in a directory.
     * 
     * @param directory the directory to scan
     * @param pattern file pattern (e.g., "*.adoc")
     * @param recursive whether to scan subdirectories
     * @param config the linter configuration
     * @return map of file to validation result
     * @throws IOException if the directory cannot be read
     */
    public Map<Path, ValidationResult> validateDirectory(Path directory, String pattern, 
                                                       boolean recursive, LinterConfiguration config) throws IOException {
        Objects.requireNonNull(directory, "directory must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        Objects.requireNonNull(config, "config must not be null");
        
        if (!Files.isDirectory(directory)) {
            throw new IOException("Not a directory: " + directory);
        }
        
        List<Path> files = findMatchingFiles(directory, pattern, recursive);
        return validateFiles(files, config);
    }
    
    /**
     * Closes the linter and releases resources.
     */
    public void close() {
        if (asciidoctor != null) {
            asciidoctor.close();
        }
    }
    
    private ValidationResult performValidation(Path file, LinterConfiguration config) {
        ValidationResult.Builder resultBuilder = ValidationResult.builder();
        
        try {
            // Parse the document
            Document document = asciidoctor.loadFile(file.toFile(), org.asciidoctor.Options.builder().build());
            
            // Run validators
            List<ValidationMessage> messages = new ArrayList<>();
            
            if (config.document() != null) {
                // Metadata validation
                if (config.document().metadata() != null) {
                    MetadataValidator metadataValidator = MetadataValidator.builder()
                        .configuration(config.document().metadata())
                        .build();
                    ValidationResult metadataResult = metadataValidator.validate(document);
                    messages.addAll(metadataResult.getMessages());
                }
                
                // Section validation
                if (config.document().sections() != null) {
                    SectionValidator sectionValidator = SectionValidator.builder()
                        .configuration(config.document())
                        .build();
                    ValidationResult sectionResult = sectionValidator.validate(document);
                    messages.addAll(sectionResult.getMessages());
                    
                    // Block validation within sections
                    messages.addAll(validateBlocks(document, config.document().sections()));
                }
            }
            
            // Add all messages to result
            messages.forEach(resultBuilder::addMessage);
            
        } catch (Exception e) {
            // Add error message for parsing failure
            resultBuilder.addMessage(createParseErrorMessage(file, e));
        }
        
        return resultBuilder.complete().build();
    }
    
    private List<ValidationMessage> validateBlocks(Document document, List<SectionConfig> sectionConfigs) {
        List<ValidationMessage> messages = new ArrayList<>();
        BlockValidator blockValidator = new BlockValidator();
        String filename = extractDocumentFilename(document);
        
        // Validate blocks in each section
        for (StructuralNode node : document.getBlocks()) {
            if (node instanceof org.asciidoctor.ast.Section) {
                org.asciidoctor.ast.Section section = (org.asciidoctor.ast.Section) node;
                
                // Find matching section config
                for (SectionConfig sectionConfig : sectionConfigs) {
                    if (matchesSection(section, sectionConfig)) {
                        ValidationResult blockResult = blockValidator.validate(section, sectionConfig, filename);
                        messages.addAll(blockResult.getMessages());
                        break;
                    }
                }
            }
        }
        
        return messages;
    }
    
    private List<Path> findMatchingFiles(Path directory, String pattern, boolean recursive) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        PathMatcher pathMatcher = directory.getFileSystem().getPathMatcher("glob:" + pattern);
        int maxDepth = recursive ? Integer.MAX_VALUE : 1;
        
        Files.walkFileTree(directory, new HashSet<>(), maxDepth, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                if (pathMatcher.matches(file.getFileName())) {
                    matchingFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
            
            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                // Log warning but continue
                System.err.println("Warning: Could not access file: " + file + " (" + exc.getMessage() + ")");
                return FileVisitResult.CONTINUE;
            }
        });
        
        return matchingFiles;
    }
    
    private String extractDocumentFilename(Document document) {
        Map<String, Object> attrs = document.getAttributes();
        if (attrs.containsKey("docfile")) {
            return attrs.get("docfile").toString();
        }
        return "unknown";
    }
    
    private boolean matchesSection(org.asciidoctor.ast.Section section, SectionConfig config) {
        // Match by title pattern if configured
        if (config.title() != null && config.title().pattern() != null) {
            String title = section.getTitle();
            if (title != null) {
                return title.matches(config.title().pattern());
            }
        }
        
        // Match by level
        return config.level() == section.getLevel();
    }
    
    private ValidationResult createIOErrorResult(Path file, IOException e) {
        return ValidationResult.builder()
            .addMessage(ValidationMessage.builder()
                .severity(com.example.linter.config.Severity.ERROR)
                .ruleId("io-error")
                .location(SourceLocation.builder()
                    .filename(file.toString())
                    .startLine(1)
                    .build())
                .message("I/O error: " + e.getMessage())
                .build())
            .complete()
            .build();
    }
    
    private ValidationMessage createParseErrorMessage(Path file, Exception e) {
        return ValidationMessage.builder()
            .severity(com.example.linter.config.Severity.ERROR)
            .ruleId("parse-error")
            .location(SourceLocation.builder()
                .filename(file.toString())
                .startLine(1)
                .build())
            .message("Failed to parse AsciiDoc file: " + e.getMessage())
            .build();
    }
}