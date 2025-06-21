package com.example.linter.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@DisplayName("FileDiscoveryService")
class FileDiscoveryServiceTest {
    
    @TempDir
    Path tempDir;
    
    private FileDiscoveryService service;
    
    @BeforeEach
    void setUp() {
        service = new FileDiscoveryService();
    }
    
    @Test
    @DisplayName("should return single file when input is file")
    void shouldReturnSingleFileWhenInputIsFile() throws IOException {
        // Given
        Path file = tempDir.resolve("test.adoc");
        Files.createFile(file);
        
        CLIConfig config = CLIConfig.builder()
            .input(file)
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertEquals(1, files.size());
        assertEquals(file, files.get(0));
    }
    
    @Test
    @DisplayName("should find matching files in directory")
    void shouldFindMatchingFilesInDirectory() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("doc1.adoc"));
        Files.createFile(tempDir.resolve("doc2.adoc"));
        Files.createFile(tempDir.resolve("readme.txt"));
        
        CLIConfig config = CLIConfig.builder()
            .input(tempDir)
            .pattern("*.adoc")
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertEquals(2, files.size());
        assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("doc1.adoc")));
        assertTrue(files.stream().anyMatch(p -> p.getFileName().toString().equals("doc2.adoc")));
    }
    
    @Test
    @DisplayName("should find files recursively")
    void shouldFindFilesRecursively() throws IOException {
        // Given
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.createFile(tempDir.resolve("root.adoc"));
        Files.createFile(subDir.resolve("nested.adoc"));
        
        CLIConfig config = CLIConfig.builder()
            .input(tempDir)
            .pattern("*.adoc")
            .recursive(true)
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertEquals(2, files.size());
    }
    
    @Test
    @DisplayName("should not find files recursively when disabled")
    void shouldNotFindFilesRecursivelyWhenDisabled() throws IOException {
        // Given
        Path subDir = tempDir.resolve("subdir");
        Files.createDirectory(subDir);
        Files.createFile(tempDir.resolve("root.adoc"));
        Files.createFile(subDir.resolve("nested.adoc"));
        
        CLIConfig config = CLIConfig.builder()
            .input(tempDir)
            .pattern("*.adoc")
            .recursive(false)
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertEquals(1, files.size());
        assertEquals("root.adoc", files.get(0).getFileName().toString());
    }
    
    @Test
    @DisplayName("should handle custom patterns")
    void shouldHandleCustomPatterns() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("doc.adoc"));
        Files.createFile(tempDir.resolve("manual.asciidoc"));
        Files.createFile(tempDir.resolve("readme.asc"));
        
        CLIConfig config = CLIConfig.builder()
            .input(tempDir)
            .pattern("*.asciidoc")
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertEquals(1, files.size());
        assertEquals("manual.asciidoc", files.get(0).getFileName().toString());
    }
    
    @Test
    @DisplayName("should throw exception for non-existent input")
    void shouldThrowExceptionForNonExistentInput() {
        // Given
        Path nonExistent = tempDir.resolve("non-existent");
        CLIConfig config = CLIConfig.builder()
            .input(nonExistent)
            .build();
        
        // When/Then
        assertThrows(IOException.class, () -> service.discoverFiles(config));
    }
    
    @Test
    @DisplayName("should return empty list when no files match")
    void shouldReturnEmptyListWhenNoFilesMatch() throws IOException {
        // Given
        Files.createFile(tempDir.resolve("readme.txt"));
        
        CLIConfig config = CLIConfig.builder()
            .input(tempDir)
            .pattern("*.adoc")
            .build();
        
        // When
        List<Path> files = service.discoverFiles(config);
        
        // Then
        assertTrue(files.isEmpty());
    }
}