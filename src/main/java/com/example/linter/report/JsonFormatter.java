package com.example.linter.report;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;
import com.google.gson.FormattingStyle;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Formats validation results as JSON.
 * Supports both pretty-printed and compact (single-line) output formats.
 */
public class JsonFormatter implements ReportFormatter {
    
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    
    private final String name;
    private final Gson gson;
    
    /**
     * Creates a JSON formatter with the specified name and formatting style.
     * 
     * @param name the formatter name (e.g., "json" or "json-compact")
     * @param style the formatting style (pretty or compact)
     */
    public JsonFormatter(String name, FormattingStyle style) {
        this.name = name;
        this.gson = new GsonBuilder()
            .setFormattingStyle(style)
            .disableHtmlEscaping()
            .create();
    }
    
    /**
     * Creates a pretty-printing JSON formatter.
     * 
     * @return a formatter that produces human-readable JSON
     */
    public static JsonFormatter pretty() {
        return new JsonFormatter("json", FormattingStyle.PRETTY);
    }
    
    /**
     * Creates a compact JSON formatter.
     * 
     * @return a formatter that produces single-line JSON
     */
    public static JsonFormatter compact() {
        return new JsonFormatter("json-compact", FormattingStyle.COMPACT);
    }
    
    @Override
    public void format(ValidationResult result, PrintWriter writer) {
        JsonObject root = new JsonObject();
        
        // Timestamp
        root.addProperty("timestamp", ISO_FORMATTER.format(Instant.now()));
        
        // Duration
        root.addProperty("duration", formatDuration(result.getValidationTimeMillis()));
        
        // Summary
        JsonObject summary = new JsonObject();
        summary.addProperty("totalMessages", result.getMessages().size());
        summary.addProperty("errors", result.getErrorCount());
        summary.addProperty("warnings", result.getWarningCount());
        summary.addProperty("infos", result.getInfoCount());
        root.add("summary", summary);
        
        // Messages
        JsonArray messages = new JsonArray();
        for (ValidationMessage msg : result.getMessages()) {
            JsonObject msgObj = new JsonObject();
            msgObj.addProperty("file", msg.getLocation().getFilename());
            msgObj.addProperty("line", msg.getLocation().getStartLine());
            
            if (msg.getLocation().getStartColumn() > 1) {
                msgObj.addProperty("column", msg.getLocation().getStartColumn());
            }
            
            msgObj.addProperty("severity", msg.getSeverity().toString());
            msgObj.addProperty("message", msg.getMessage());
            
            // Optional fields
            if (msg.getRuleId() != null) {
                msgObj.addProperty("ruleId", msg.getRuleId());
            }
            
            msg.getActualValue().ifPresent(value -> 
                msgObj.addProperty("actualValue", value));
            
            msg.getExpectedValue().ifPresent(value -> 
                msgObj.addProperty("expectedValue", value));
            
            messages.add(msgObj);
        }
        root.add("messages", messages);
        
        // Write JSON to PrintWriter
        gson.toJson(root, writer);
        writer.flush();
    }
    
    private String formatDuration(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        } else {
            return String.format("%.3fs", millis / 1000.0);
        }
    }
    
    @Override
    public String getName() {
        return name;
    }
}