package com.example.linter.report;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.example.linter.config.Severity;
import com.example.linter.config.output.OutputConfiguration;
import com.example.linter.config.output.OutputFormat;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;

/**
 * Tests for ReportWriter with OutputConfiguration support.
 */
@DisplayName("ReportWriter OutputConfiguration Tests")
class ReportWriterOutputConfigTest {
    
    private ReportWriter reportWriter;
    private ValidationResult testResult;
    
    @BeforeEach
    void setUp() {
        reportWriter = new ReportWriter();
        
        // Create test result
        testResult = ValidationResult.builder()
            .addMessage(ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("test.rule")
                .message("Test error message")
                .location(SourceLocation.builder()
                    .filename("test.adoc")
                    .line(10)
                    .column(5)
                    .build())
                .build())
            .complete()
            .build();
    }
    
    @Nested
    @DisplayName("Console Output Tests")
    class ConsoleOutputTests {
        
        @Test
        @DisplayName("should use default console formatter when no output config provided")
        void shouldUseDefaultConsoleFormatterWhenNoOutputConfigProvided() {
            // Given
            StringWriter output = new StringWriter();
            PrintWriter writer = new PrintWriter(output);
            
            // When
            reportWriter.write(testResult, "console", writer);
            
            // Then
            String result = output.toString();
            assertTrue(result.contains("test.adoc:10:5"));
            assertTrue(result.contains("ERROR"));
            assertTrue(result.contains("test.rule"));
            assertTrue(result.contains("Test error message"));
        }
        
        @Test
        @DisplayName("should use enhanced output config when provided")
        void shouldUseEnhancedOutputConfigWhenProvided() {
            // Given
            StringWriter output = new StringWriter();
            PrintWriter writer = new PrintWriter(output);
            OutputConfiguration outputConfig = OutputConfiguration.defaultConfig();
            
            // When
            reportWriter.write(testResult, "console", writer, outputConfig);
            
            // Then
            String result = output.toString();
            assertTrue(result.contains("test.adoc:10:5"));
            assertTrue(result.contains("ERROR"));
            assertTrue(result.contains("test.rule"));
            assertTrue(result.contains("Test error message"));
            // Enhanced format includes summary
            assertTrue(result.contains("Validation Summary"));
        }
        
        @Test
        @DisplayName("should use compact output config when provided")
        void shouldUseCompactOutputConfigWhenProvided() {
            // Given
            StringWriter output = new StringWriter();
            PrintWriter writer = new PrintWriter(output);
            OutputConfiguration outputConfig = OutputConfiguration.compactConfig();
            
            // When
            reportWriter.write(testResult, "console", writer, outputConfig);
            
            // Then
            String result = output.toString();
            assertTrue(result.contains("test.adoc:10:5"));
            // Compact format should be single line
            assertEquals(1, result.trim().split("\n").length);
        }
    }
    
    @Nested
    @DisplayName("File Output Tests")
    class FileOutputTests {
        
        @TempDir
        Path tempDir;
        
        @Test
        @DisplayName("should write to file with output config")
        void shouldWriteToFileWithOutputConfig() throws IOException {
            // Given
            Path outputFile = tempDir.resolve("report.txt");
            OutputConfiguration outputConfig = OutputConfiguration.builder()
                .format(OutputFormat.SIMPLE)
                .build();
            
            // When
            reportWriter.write(testResult, "console", outputFile.toString(), outputConfig);
            
            // Then
            assertTrue(Files.exists(outputFile));
            String content = Files.readString(outputFile);
            assertTrue(content.contains("test.adoc:10:5"));
            assertTrue(content.contains("ERROR"));
        }
    }
    
    @Nested
    @DisplayName("WriteToConsole Method Tests")
    class WriteToConsoleMethodTests {
        
        @Test
        @DisplayName("should write to console with default config")
        void shouldWriteToConsoleWithDefaultConfig() {
            // When/Then - just verify no exception
            assertDoesNotThrow(() -> 
                reportWriter.writeToConsole(testResult, "console")
            );
        }
        
        @Test
        @DisplayName("should write to console with custom output config")
        void shouldWriteToConsoleWithCustomOutputConfig() {
            // Given
            OutputConfiguration outputConfig = OutputConfiguration.builder()
                .format(OutputFormat.ENHANCED)
                .build();
            
            // When/Then - just verify no exception
            assertDoesNotThrow(() -> 
                reportWriter.writeToConsole(testResult, "console", outputConfig)
            );
        }
    }
    
    @Nested
    @DisplayName("JSON Format Tests")
    class JsonFormatTests {
        
        @Test
        @DisplayName("should ignore output config for JSON format")
        void shouldIgnoreOutputConfigForJsonFormat() {
            // Given
            StringWriter output1 = new StringWriter();
            StringWriter output2 = new StringWriter();
            PrintWriter writer1 = new PrintWriter(output1);
            PrintWriter writer2 = new PrintWriter(output2);
            
            OutputConfiguration outputConfig = OutputConfiguration.defaultConfig();
            
            // When
            reportWriter.write(testResult, "json", writer1);
            reportWriter.write(testResult, "json", writer2, outputConfig);
            
            // Then - JSON output should be the same regardless of output config
            assertEquals(output1.toString(), output2.toString());
            assertTrue(output1.toString().contains("\"severity\":\"ERROR\""));
        }
    }
}