package com.example.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.LiteralBlock;
import com.example.linter.validator.ValidationMessage;

/**
 * Validator for literal blocks in AsciiDoc documents.
 * 
 * <p>This validator validates literal blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/literal-block.yaml}.
 * The YAML configuration is parsed into {@link LiteralBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Literal blocks use .... delimiters and display preformatted text without syntax highlighting.
 * They are commonly used for configuration files, console output, or other plain text content.</p>
 * 
 * <p>Example usage in AsciiDoc:</p>
 * <pre>
 * .Example Configuration
 * ....
 * server:
 *   host: localhost
 *   port: 8080
 *   timeout: 30s
 * ....
 * </pre>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>title</b>: Validates optional title (required, minLength, maxLength)</li>
 *   <li><b>lines</b>: Validates line count (min, max)</li>
 *   <li><b>indentation</b>: Validates indentation consistency and constraints (required, consistent, minSpaces, maxSpaces)</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see LiteralBlock
 * @see BlockTypeValidator
 */
public final class LiteralBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.LITERAL;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          Block config,
                                          BlockValidationContext context) {
        
        LiteralBlock literalConfig = (LiteralBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get literal block attributes and content
        String title = getTitle(block);
        List<String> lines = getContentLines(block);
        
        // Validate title
        if (literalConfig.getTitle() != null) {
            validateTitle(title, literalConfig.getTitle(), literalConfig, context, block, messages);
        }
        
        // Validate lines
        if (literalConfig.getLines() != null) {
            validateLines(lines, literalConfig.getLines(), literalConfig, context, block, messages);
        }
        
        // Validate indentation
        if (literalConfig.getIndentation() != null) {
            validateIndentation(lines, literalConfig.getIndentation(), literalConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getTitle(StructuralNode block) {
        Object titleObj = block.getTitle();
        return titleObj != null ? titleObj.toString() : null;
    }
    
    private List<String> getContentLines(StructuralNode block) {
        List<String> lines = new ArrayList<>();
        
        // For literal blocks, content is the primary source
        if (block.getContent() != null) {
            String content = block.getContent().toString();
            if (!content.isEmpty()) {
                String[] contentLines = content.split("\n");
                for (String line : contentLines) {
                    lines.add(line);
                }
            }
        }
        
        // Try blocks as fallback
        if (lines.isEmpty() && block.getBlocks() != null && !block.getBlocks().isEmpty()) {
            for (StructuralNode child : block.getBlocks()) {
                if (child.getContent() != null) {
                    String childContent = child.getContent().toString();
                    String[] childLines = childContent.split("\n");
                    for (String line : childLines) {
                        lines.add(line);
                    }
                }
            }
        }
        
        return lines;
    }
    
    private void validateTitle(String title, LiteralBlock.TitleConfig config,
                             LiteralBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if title is required
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("literal.title.required")
                .location(context.createLocation(block))
                .message("Literal block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null && !title.trim().isEmpty()) {
            int titleLength = title.trim().length();
            
            // Validate min length
            if (config.getMinLength() != null && titleLength < config.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("literal.title.minLength")
                    .location(context.createLocation(block))
                    .message("Literal block title is too short")
                    .actualValue(titleLength + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && titleLength > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("literal.title.maxLength")
                    .location(context.createLocation(block))
                    .message("Literal block title is too long")
                    .actualValue(titleLength + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateLines(List<String> lines, LiteralBlock.LinesConfig config,
                             LiteralBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int lineCount = lines.size();
        
        // Validate min lines
        if (config.getMin() != null && lineCount < config.getMin()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("literal.lines.min")
                .location(context.createLocation(block))
                .message("Literal block has too few lines")
                .actualValue(lineCount + " lines")
                .expectedValue("At least " + config.getMin() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.getMax() != null && lineCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("literal.lines.max")
                .location(context.createLocation(block))
                .message("Literal block has too many lines")
                .actualValue(lineCount + " lines")
                .expectedValue("At most " + config.getMax() + " lines")
                .build());
        }
    }
    
    private void validateIndentation(List<String> lines, LiteralBlock.IndentationConfig config,
                                   LiteralBlock blockConfig,
                                   BlockValidationContext context,
                                   StructuralNode block,
                                   List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Skip if indentation checking is not required
        if (!config.isRequired()) {
            return;
        }
        
        Integer firstIndentation = null;
        int lineNumber = 0;
        
        for (String line : lines) {
            lineNumber++;
            
            // Skip empty lines for indentation checking
            if (line.isEmpty() || line.trim().isEmpty()) {
                continue;
            }
            
            int indentSpaces = countLeadingSpaces(line);
            
            // Check minimum spaces
            if (config.getMinSpaces() != null && indentSpaces < config.getMinSpaces()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("literal.indentation.minSpaces")
                    .location(context.createLocation(block))
                    .message("Line " + lineNumber + " has insufficient indentation")
                    .actualValue(indentSpaces + " spaces")
                    .expectedValue("At least " + config.getMinSpaces() + " spaces")
                    .build());
            }
            
            // Check maximum spaces
            if (config.getMaxSpaces() != null && indentSpaces > config.getMaxSpaces()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("literal.indentation.maxSpaces")
                    .location(context.createLocation(block))
                    .message("Line " + lineNumber + " has excessive indentation")
                    .actualValue(indentSpaces + " spaces")
                    .expectedValue("At most " + config.getMaxSpaces() + " spaces")
                    .build());
            }
            
            // Check consistency
            if (config.isConsistent()) {
                if (firstIndentation == null) {
                    firstIndentation = indentSpaces;
                } else if (indentSpaces != firstIndentation) {
                    messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("literal.indentation.consistent")
                        .location(context.createLocation(block))
                        .message("Line " + lineNumber + " has inconsistent indentation")
                        .actualValue(indentSpaces + " spaces")
                        .expectedValue(firstIndentation + " spaces (consistent with first non-empty line)")
                        .build());
                }
            }
        }
    }
    
    private int countLeadingSpaces(String line) {
        int count = 0;
        for (char c : line.toCharArray()) {
            if (c == ' ') {
                count++;
            } else if (c == '\t') {
                // Count tab as 4 spaces (configurable in future)
                count += 4;
            } else {
                break;
            }
        }
        return count;
    }
}