package com.example.linter.report;

import java.io.PrintWriter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.example.linter.config.Severity;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;

/**
 * Formats validation results for console output.
 * Supports ANSI color codes for different severity levels when outputting to a terminal.
 */
public class ConsoleFormatter implements ReportFormatter {
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    
    private final boolean useColors;
    
    public ConsoleFormatter() {
        this(System.console() != null);
    }
    
    public ConsoleFormatter(boolean useColors) {
        this.useColors = useColors;
    }
    
    @Override
    public void format(ValidationResult result, PrintWriter writer) {
        writer.println("Validation Report");
        writer.println("=================");
        writer.println();
        
        if (result.getMessages().isEmpty()) {
            writer.println("No validation issues found.");
        } else {
            formatMessages(result, writer);
        }
        
        writer.println();
        formatSummary(result, writer);
    }
    
    private void formatMessages(ValidationResult result, PrintWriter writer) {
        Map<String, List<ValidationMessage>> messagesByFile = result.getMessagesByFile();
        
        for (Map.Entry<String, List<ValidationMessage>> entry : messagesByFile.entrySet()) {
            String filename = entry.getKey();
            List<ValidationMessage> fileMessages = entry.getValue();
            
            // Sort messages by line and column
            fileMessages.sort(Comparator
                .comparing((ValidationMessage msg) -> msg.getLocation().getStartLine())
                .thenComparing(msg -> msg.getLocation().getStartColumn()));
            
            writer.println(filename + ":");
            
            for (ValidationMessage msg : fileMessages) {
                formatMessage(msg, writer);
            }
            
            writer.println();
        }
    }
    
    private void formatMessage(ValidationMessage msg, PrintWriter writer) {
        String severityLabel = formatSeverity(msg.getSeverity());
        String location = String.format("  Line %d", msg.getLocation().getStartLine());
        
        if (msg.getLocation().getStartColumn() > 0) {
            location += String.format(", Column %d", msg.getLocation().getStartColumn());
        }
        
        writer.println(location + ": " + severityLabel + " " + msg.getMessage());
        
        if (msg.getRuleId() != null) {
            writer.println("    Rule: " + msg.getRuleId());
        }
        
        if (msg.getActualValue().isPresent() || msg.getExpectedValue().isPresent()) {
            msg.getActualValue().ifPresent(value -> 
                writer.println("    Actual: " + value));
            msg.getExpectedValue().ifPresent(value -> 
                writer.println("    Expected: " + value));
        }
    }
    
    private String formatSeverity(Severity severity) {
        if (!useColors) {
            return "[" + severity + "]";
        }
        
        return switch (severity) {
            case ERROR -> ANSI_RED + "[ERROR]" + ANSI_RESET;
            case WARN -> ANSI_YELLOW + "[WARN]" + ANSI_RESET;
            case INFO -> ANSI_BLUE + "[INFO]" + ANSI_RESET;
        };
    }
    
    private void formatSummary(ValidationResult result, PrintWriter writer) {
        int errors = result.getErrorCount();
        int warnings = result.getWarningCount();
        int infos = result.getInfoCount();
        
        String summary = String.format("Summary: %d error%s, %d warning%s, %d info message%s",
            errors, errors == 1 ? "" : "s",
            warnings, warnings == 1 ? "" : "s",
            infos, infos == 1 ? "" : "s");
        
        if (useColors && errors > 0) {
            summary = ANSI_RED + summary + ANSI_RESET;
        } else if (useColors && warnings > 0) {
            summary = ANSI_YELLOW + summary + ANSI_RESET;
        }
        
        writer.println(summary);
        writer.println("Validation completed in " + result.getValidationTimeMillis() + "ms");
    }
    
    @Override
    public String getName() {
        return "console";
    }
}