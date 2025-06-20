package com.example.linter.report;

import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Formats validation results as JSON.
 * Produces a structured JSON output suitable for programmatic consumption.
 */
public class JsonFormatter implements ReportFormatter {
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    
    @Override
    public void format(ValidationResult result, PrintWriter writer) {
        writer.println("{");
        
        // Timestamp
        writer.println("  \"timestamp\": \"" + ISO_FORMATTER.format(Instant.now()) + "\",");
        
        // Duration
        writer.println("  \"duration\": \"" + formatDuration(result.getValidationTimeMillis()) + "\",");
        
        // Summary
        writer.println("  \"summary\": {");
        writer.println("    \"totalMessages\": " + result.getMessages().size() + ",");
        writer.println("    \"errors\": " + result.getErrorCount() + ",");
        writer.println("    \"warnings\": " + result.getWarningCount() + ",");
        writer.println("    \"infos\": " + result.getInfoCount());
        writer.println("  },");
        
        // Messages
        writer.println("  \"messages\": [");
        formatMessages(result.getMessages(), writer);
        writer.println("  ]");
        
        writer.println("}");
    }
    
    private void formatMessages(List<ValidationMessage> messages, PrintWriter writer) {
        for (int i = 0; i < messages.size(); i++) {
            ValidationMessage msg = messages.get(i);
            boolean isLast = (i == messages.size() - 1);
            
            writer.println("    {");
            writer.println("      \"file\": \"" + escapeJson(msg.getLocation().getFilename()) + "\",");
            writer.println("      \"line\": " + msg.getLocation().getStartLine() + ",");
            
            if (msg.getLocation().getStartColumn() > 0) {
                writer.println("      \"column\": " + msg.getLocation().getStartColumn() + ",");
            }
            
            writer.println("      \"severity\": \"" + msg.getSeverity() + "\",");
            writer.println("      \"message\": \"" + escapeJson(msg.getMessage()) + "\"");
            
            // Optional fields
            if (msg.getRuleId() != null) {
                writer.println(",");
                writer.println("      \"ruleId\": \"" + escapeJson(msg.getRuleId()) + "\"");
            }
            
            if (msg.getActualValue().isPresent()) {
                writer.println(",");
                writer.println("      \"actualValue\": \"" + escapeJson(msg.getActualValue().orElse("")) + "\"");
            }
            
            if (msg.getExpectedValue().isPresent()) {
                writer.println(",");
                writer.println("      \"expectedValue\": \"" + escapeJson(msg.getExpectedValue().orElse("")) + "\"");
            }
            
            writer.print("    }");
            if (!isLast) {
                writer.print(",");
            }
            writer.println();
        }
    }
    
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else {
            return String.format("%.3fs", millis / 1000.0);
        }
    }
    
    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        
        return value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }
    
    @Override
    public String getName() {
        return "json";
    }
}