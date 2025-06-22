package com.example.linter.report.console;

import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.example.linter.config.output.DisplayConfig;
import com.example.linter.config.output.SummaryConfig;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.ValidationResult;

/**
 * Renders an enhanced validation summary with statistics and visualizations.
 */
public class SummaryRenderer {
    private static final int MAX_BAR_WIDTH = 20;
    private static final String BAR_CHAR = "█";
    
    private final SummaryConfig config;
    private final ColorScheme colorScheme;
    
    public SummaryRenderer(SummaryConfig config, DisplayConfig displayConfig) {
        this.config = Objects.requireNonNull(config, "config must not be null");
        this.colorScheme = new ColorScheme(displayConfig.isUseColors());
    }
    
    /**
     * Renders the validation summary.
     */
    public void render(ValidationResult result, PrintWriter writer) {
        if (!config.isEnabled()) {
            return;
        }
        
        writer.println();
        writer.println(colorScheme.separator("═".repeat(65)));
        writer.println(colorScheme.header("                    Validation Summary"));
        writer.println(colorScheme.separator("═".repeat(65)));
        
        if (config.isShowStatistics()) {
            renderStatistics(result, writer);
        }
        
        if (config.isShowMostCommon()) {
            renderMostCommonIssues(result, writer);
        }
        
        if (config.isShowFileList()) {
            renderFileList(result, writer);
        }
        
        renderSummaryLine(result, writer);
        
        writer.println(colorScheme.separator("═".repeat(65)));
    }
    
    private void renderStatistics(ValidationResult result, PrintWriter writer) {
        // File statistics
        int totalFiles = getUniqueFileCount(result);
        int filesWithErrors = getFilesWithErrorCount(result);
        
        writer.println("  Total files scanned:     " + totalFiles);
        writer.println("  Files with errors:       " + filesWithErrors);
        writer.println();
        
        // Error counts with visual bars
        int errors = result.getErrorCount();
        int warnings = result.getWarningCount();
        int infos = result.getInfoCount();
        int total = errors + warnings + infos;
        
        if (total > 0) {
            writer.println("  " + formatCountWithBar("Errors", errors, total, colorScheme::errorBar));
            writer.println("  " + formatCountWithBar("Warnings", warnings, total, colorScheme::warningBar));
            writer.println("  " + formatCountWithBar("Info", infos, total, colorScheme::infoBar));
            writer.println();
        }
    }
    
    private void renderMostCommonIssues(ValidationResult result, PrintWriter writer) {
        Map<String, Long> issueFrequency = result.getMessages().stream()
            .collect(Collectors.groupingBy(
                ValidationMessage::getRuleId,
                LinkedHashMap::new,
                Collectors.counting()
            ));
        
        if (issueFrequency.isEmpty()) {
            return;
        }
        
        writer.println("  Most common issues:");
        
        // Get top 5 issues
        issueFrequency.entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                String ruleId = entry.getKey();
                long count = entry.getValue();
                
                // Try to get a description from the first message with this rule
                String description = result.getMessages().stream()
                    .filter(msg -> msg.getRuleId().equals(ruleId))
                    .findFirst()
                    .map(msg -> extractShortDescription(msg.getMessage()))
                    .orElse(ruleId);
                
                writer.printf("  • %s (%d occurrence%s)%n",
                    description,
                    count,
                    count == 1 ? "" : "s"
                );
            });
        
        writer.println();
        
        // Auto-fix hint
        long autoFixableCount = result.getMessages().stream()
            .filter(ValidationMessage::hasAutoFixableSuggestions)
            .count();
        
        if (autoFixableCount > 0) {
            writer.println(colorScheme.suggestionIcon("  💡 ") + 
                autoFixableCount + " error" + (autoFixableCount == 1 ? " is" : "s are") + 
                " auto-fixable. Run with --fix");
            writer.println();
        }
    }
    
    private void renderFileList(ValidationResult result, PrintWriter writer) {
        Map<String, List<ValidationMessage>> byFile = result.getMessagesByFile();
        
        if (byFile.isEmpty()) {
            return;
        }
        
        writer.println("  Files with issues:");
        byFile.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String filename = entry.getKey();
                List<ValidationMessage> messages = entry.getValue();
                
                long errorCount = messages.stream()
                    .filter(msg -> msg.getSeverity() == com.example.linter.config.Severity.ERROR)
                    .count();
                long warnCount = messages.stream()
                    .filter(msg -> msg.getSeverity() == com.example.linter.config.Severity.WARN)
                    .count();
                
                writer.printf("  - %s: ", filename);
                if (errorCount > 0) {
                    writer.print(colorScheme.error(errorCount + " error" + (errorCount == 1 ? "" : "s")));
                    if (warnCount > 0) {
                        writer.print(", ");
                    }
                }
                if (warnCount > 0) {
                    writer.print(colorScheme.warning(warnCount + " warning" + (warnCount == 1 ? "" : "s")));
                }
                writer.println();
            });
        writer.println();
    }
    
    private void renderSummaryLine(ValidationResult result, PrintWriter writer) {
        int errors = result.getErrorCount();
        int warnings = result.getWarningCount();
        int infos = result.getInfoCount();
        
        String summary = String.format("Summary: %d error%s, %d warning%s, %d info message%s",
            errors, errors == 1 ? "" : "s",
            warnings, warnings == 1 ? "" : "s",
            infos, infos == 1 ? "" : "s");
        
        // Color based on severity
        if (errors > 0) {
            summary = colorScheme.error(summary);
        } else if (warnings > 0) {
            summary = colorScheme.warning(summary);
        } else {
            summary = colorScheme.success(summary);
        }
        
        writer.println();
        writer.println(summary);
        writer.println("Validation completed in " + result.getValidationTimeMillis() + "ms");
    }
    
    private String formatCountWithBar(String label, int count, int total, 
                                    java.util.function.Function<String, String> colorizer) {
        // Calculate bar width proportionally
        int barWidth = total > 0 ? (count * MAX_BAR_WIDTH) / total : 0;
        barWidth = Math.max(barWidth, count > 0 ? 1 : 0); // At least 1 if count > 0
        
        String bar = BAR_CHAR.repeat(barWidth);
        return String.format("%-9s %3d %s", label + ":", count, colorizer.apply(bar));
    }
    
    private int getUniqueFileCount(ValidationResult result) {
        return (int) result.getMessages().stream()
            .map(msg -> msg.getLocation().getFilename())
            .distinct()
            .count();
    }
    
    private int getFilesWithErrorCount(ValidationResult result) {
        return (int) result.getMessages().stream()
            .filter(msg -> msg.getSeverity() == com.example.linter.config.Severity.ERROR)
            .map(msg -> msg.getLocation().getFilename())
            .distinct()
            .count();
    }
    
    private String extractShortDescription(String message) {
        // Try to shorten long messages
        if (message.length() > 50) {
            int cutoff = message.indexOf(" at ");
            if (cutoff > 0) {
                return message.substring(0, cutoff);
            }
            cutoff = message.indexOf(" in ");
            if (cutoff > 0) {
                return message.substring(0, cutoff);
            }
            return message.substring(0, 47) + "...";
        }
        return message;
    }
}