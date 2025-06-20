package com.example.linter.report;

import com.example.linter.validator.ValidationResult;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Facade for writing validation reports in different formats.
 * Manages available formatters and handles output to files or console.
 */
public class ReportWriter {
    
    private final Map<String, ReportFormatter> formatters;
    
    public ReportWriter() {
        this.formatters = new HashMap<>();
        registerDefaultFormatters();
    }
    
    private void registerDefaultFormatters() {
        registerFormatter(new ConsoleFormatter());
        registerFormatter(new JsonFormatter());
    }
    
    /**
     * Registers a formatter for use by this writer.
     * 
     * @param formatter the formatter to register
     */
    public void registerFormatter(ReportFormatter formatter) {
        Objects.requireNonNull(formatter, "formatter must not be null");
        formatters.put(formatter.getName(), formatter);
    }
    
    /**
     * Writes the validation result using the specified format.
     * If outputPath is null, writes to standard output.
     * 
     * @param result the validation result to write
     * @param format the output format (e.g., "console", "json")
     * @param outputPath the output file path, or null for standard output
     * @throws IOException if an I/O error occurs
     * @throws IllegalArgumentException if the format is not supported
     */
    public void write(ValidationResult result, String format, String outputPath) throws IOException {
        Objects.requireNonNull(result, "result must not be null");
        
        ReportFormatter formatter = getFormatter(format);
        
        if (outputPath == null) {
            writeToConsole(result, formatter);
        } else {
            writeToFile(result, formatter, outputPath);
        }
    }
    
    /**
     * Writes the validation result using the specified format to a Path.
     * 
     * @param result the validation result to write
     * @param format the output format
     * @param outputPath the output file path
     * @throws IOException if an I/O error occurs
     */
    public void write(ValidationResult result, String format, Path outputPath) throws IOException {
        write(result, format, outputPath != null ? outputPath.toString() : null);
    }
    
    private void writeToConsole(ValidationResult result, ReportFormatter formatter) {
        try (PrintWriter writer = new PrintWriter(System.out)) {
            formatter.format(result, writer);
            writer.flush();
        }
    }
    
    private void writeToFile(ValidationResult result, ReportFormatter formatter, String outputPath) 
            throws IOException {
        try (PrintWriter writer = new PrintWriter(
                new FileWriter(outputPath, StandardCharsets.UTF_8))) {
            formatter.format(result, writer);
        }
    }
    
    private ReportFormatter getFormatter(String format) {
        String formatName = format != null ? format.toLowerCase() : "console";
        ReportFormatter formatter = formatters.get(formatName);
        
        if (formatter == null) {
            throw new IllegalArgumentException(
                "Unsupported format: " + format + ". Available formats: " + getAvailableFormats());
        }
        
        return formatter;
    }
    
    /**
     * Returns the set of available format names.
     * 
     * @return the available format names
     */
    public Set<String> getAvailableFormats() {
        return formatters.keySet();
    }
    
    /**
     * Calculates the exit code based on the validation result.
     * 
     * @param result the validation result
     * @return 0 if no errors, 1 if errors found
     */
    public static int calculateExitCode(ValidationResult result) {
        return result.hasErrors() ? 1 : 0;
    }
}