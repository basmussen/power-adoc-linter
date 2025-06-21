package com.example.linter.cli;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service for discovering AsciiDoc files based on patterns and recursive settings.
 */
public class FileDiscoveryService {
    
    private static final Logger logger = LogManager.getLogger(FileDiscoveryService.class);
    
    /**
     * Discovers files based on the CLI configuration.
     * 
     * @param config The CLI configuration
     * @return List of paths to validate
     * @throws IOException if an I/O error occurs
     */
    public List<Path> discoverFiles(CLIConfig config) throws IOException {
        Path input = config.getInput();
        
        if (Files.isRegularFile(input)) {
            // Single file
            return List.of(input);
        } else if (Files.isDirectory(input)) {
            // Directory - find matching files
            return findMatchingFiles(input, config.getPattern(), config.isRecursive());
        } else {
            throw new IOException("Input path does not exist or is not accessible: " + input);
        }
    }
    
    private List<Path> findMatchingFiles(Path directory, String pattern, boolean recursive) throws IOException {
        List<Path> matchingFiles = new ArrayList<>();
        PathMatcher pathMatcher = directory.getFileSystem().getPathMatcher("glob:" + pattern);
        int maxDepth = recursive ? Integer.MAX_VALUE : 1;
        
        Files.walkFileTree(directory, new java.util.HashSet<>(), maxDepth, new SimpleFileVisitor<Path>() {
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
                logger.warn("Could not access file: {} ({})", file, exc.getMessage());
                return FileVisitResult.CONTINUE;
            }
        });
        
        return matchingFiles;
    }
}