package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.blocks.TableBlock;
import com.example.linter.config.BlockType;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;
import org.asciidoctor.ast.Table;
import org.asciidoctor.ast.Row;
import org.asciidoctor.ast.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for table blocks.
 */
public final class TableBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.TABLE;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          AbstractBlock config,
                                          BlockValidationContext context) {
        
        if (!(block instanceof Table)) {
            // Should not happen if BlockTypeDetector works correctly
            return List.of();
        }
        
        Table table = (Table) block;
        TableBlock tableConfig = (TableBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate columns
        if (tableConfig.getColumns() != null) {
            validateColumns(table, tableConfig, context, messages);
        }
        
        // Validate rows
        if (tableConfig.getRows() != null) {
            validateRows(table, tableConfig, context, messages);
        }
        
        // Validate header
        if (tableConfig.getHeader() != null) {
            validateHeader(table, tableConfig, context, messages);
        }
        
        // Validate caption
        if (tableConfig.getCaption() != null) {
            validateCaption(table, tableConfig, context, messages);
        }
        
        // Validate format
        if (tableConfig.getFormat() != null) {
            validateFormat(table, tableConfig, context, messages);
        }
        
        return messages;
    }
    
    private void validateColumns(Table table, TableBlock config, 
                               BlockValidationContext context,
                               List<ValidationMessage> messages) {
        
        TableBlock.DimensionConfig columnConfig = config.getColumns();
        int columnCount = table.getColumns().size();
        
        if (columnConfig.getMin() != null && columnCount < columnConfig.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(columnConfig.getSeverity())
                .ruleId("table.columns.min")
                .location(context.createLocation(table))
                .message("Table has too few columns")
                .actualValue(String.valueOf(columnCount))
                .expectedValue("At least " + columnConfig.getMin() + " columns")
                .build());
        }
        
        if (columnConfig.getMax() != null && columnCount > columnConfig.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(columnConfig.getSeverity())
                .ruleId("table.columns.max")
                .location(context.createLocation(table))
                .message("Table has too many columns")
                .actualValue(String.valueOf(columnCount))
                .expectedValue("At most " + columnConfig.getMax() + " columns")
                .build());
        }
    }
    
    private void validateRows(Table table, TableBlock config,
                            BlockValidationContext context,
                            List<ValidationMessage> messages) {
        
        TableBlock.DimensionConfig rowConfig = config.getRows();
        int rowCount = table.getBody().size();
        
        if (rowConfig.getMin() != null && rowCount < rowConfig.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(rowConfig.getSeverity())
                .ruleId("table.rows.min")
                .location(context.createLocation(table))
                .message("Table has too few rows")
                .actualValue(String.valueOf(rowCount))
                .expectedValue("At least " + rowConfig.getMin() + " rows")
                .build());
        }
        
        if (rowConfig.getMax() != null && rowCount > rowConfig.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(rowConfig.getSeverity())
                .ruleId("table.rows.max")
                .location(context.createLocation(table))
                .message("Table has too many rows")
                .actualValue(String.valueOf(rowCount))
                .expectedValue("At most " + rowConfig.getMax() + " rows")
                .build());
        }
    }
    
    private void validateHeader(Table table, TableBlock config,
                              BlockValidationContext context,
                              List<ValidationMessage> messages) {
        
        TableBlock.HeaderConfig headerConfig = config.getHeader();
        boolean hasHeader = !table.getHeader().isEmpty();
        
        if (headerConfig.isRequired() && !hasHeader) {
            messages.add(ValidationMessage.builder()
                .severity(headerConfig.getSeverity())
                .ruleId("table.header.required")
                .location(context.createLocation(table))
                .message("Table must have a header row")
                .actualValue("No header")
                .expectedValue("Header row required")
                .build());
        }
        
        // Validate header pattern if header exists
        if (hasHeader && headerConfig.getPattern() != null) {
            Pattern pattern = headerConfig.getPattern();
            
            for (Row headerRow : table.getHeader()) {
                for (Cell cell : headerRow.getCells()) {
                    String content = cell.getText();
                    if (!pattern.matcher(content).matches()) {
                        messages.add(ValidationMessage.builder()
                            .severity(headerConfig.getSeverity())
                            .ruleId("table.header.pattern")
                            .location(context.createLocation(table))
                            .message("Table header does not match required pattern")
                            .actualValue(content)
                            .expectedValue("Pattern: " + headerConfig.getPattern())
                            .build());
                    }
                }
            }
        }
    }
    
    private void validateCaption(Table table, TableBlock config,
                               BlockValidationContext context,
                               List<ValidationMessage> messages) {
        
        TableBlock.CaptionConfig captionConfig = config.getCaption();
        String caption = table.getTitle();
        
        if (captionConfig.isRequired() && (caption == null || caption.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(captionConfig.getSeverity())
                .ruleId("table.caption.required")
                .location(context.createLocation(table))
                .message("Table must have a caption")
                .actualValue("No caption")
                .expectedValue("Caption required")
                .build());
            return;
        }
        
        if (caption != null && !caption.trim().isEmpty()) {
            // Validate caption pattern
            if (captionConfig.getPattern() != null) {
                Pattern pattern = captionConfig.getPattern();
                if (!pattern.matcher(caption).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(captionConfig.getSeverity())
                        .ruleId("table.caption.pattern")
                        .location(context.createLocation(table))
                        .message("Table caption does not match required pattern")
                        .actualValue(caption)
                        .expectedValue("Pattern: " + captionConfig.getPattern())
                        .build());
                }
            }
            
            // Validate caption length
            if (captionConfig.getMinLength() != null && caption.length() < captionConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(captionConfig.getSeverity())
                    .ruleId("table.caption.minLength")
                    .location(context.createLocation(table))
                    .message("Table caption is too short")
                    .actualValue(caption.length() + " characters")
                    .expectedValue("At least " + captionConfig.getMinLength() + " characters")
                    .build());
            }
            
            if (captionConfig.getMaxLength() != null && caption.length() > captionConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(captionConfig.getSeverity())
                    .ruleId("table.caption.maxLength")
                    .location(context.createLocation(table))
                    .message("Table caption is too long")
                    .actualValue(caption.length() + " characters")
                    .expectedValue("At most " + captionConfig.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateFormat(Table table, TableBlock config,
                              BlockValidationContext context,
                              List<ValidationMessage> messages) {
        
        TableBlock.FormatConfig formatConfig = config.getFormat();
        
        // Validate table style
        if (formatConfig.getStyle() != null) {
            Object styleObj = table.getAttribute("options");
            String actualStyle = styleObj != null ? styleObj.toString() : null;
            if (actualStyle == null || !actualStyle.contains(formatConfig.getStyle())) {
                messages.add(ValidationMessage.builder()
                    .severity(formatConfig.getSeverity())
                    .ruleId("table.format.style")
                    .location(context.createLocation(table))
                    .message("Table does not have required style")
                    .actualValue(actualStyle != null ? actualStyle : "default")
                    .expectedValue("Style: " + formatConfig.getStyle())
                    .build());
            }
        }
        
        // Validate borders
        if (formatConfig.getBorders() != null && formatConfig.getBorders()) {
            Object frameObj = table.getAttribute("frame");
            String frame = frameObj != null ? frameObj.toString() : null;
            if (frame == null || "none".equals(frame)) {
                messages.add(ValidationMessage.builder()
                    .severity(formatConfig.getSeverity())
                    .ruleId("table.format.borders")
                    .location(context.createLocation(table))
                    .message("Table must have borders")
                    .actualValue("No borders")
                    .expectedValue("Borders required")
                    .build());
            }
        }
    }
}