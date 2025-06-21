package com.example.linter.validator.block;

import java.util.ArrayList;
import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.validator.ValidationMessage;

/**
 * Validator for paragraph blocks in AsciiDoc documents.
 * 
 * <p>This validator validates paragraph blocks based on the YAML schema structure
 * defined in {@code src/main/resources/schemas/blocks/paragraph-block.yaml}.
 * The YAML configuration is parsed into {@link ParagraphBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>lines</b>: Validates line count constraints (min/max number of lines)</li>
 * </ul>
 * 
 * <p>The lines configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see ParagraphBlock
 * @see BlockTypeValidator
 */
public final class ParagraphBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.PARAGRAPH;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          Block config,
                                          BlockValidationContext context) {
        
        ParagraphBlock paragraphConfig = (ParagraphBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get paragraph content
        String content = getBlockContent(block);
        
        // Validate line count if configured
        if (paragraphConfig.getLines() != null) {
            int lineCount = countLines(content);
            validateLineCount(lineCount, paragraphConfig.getLines(), paragraphConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getBlockContent(StructuralNode block) {
        // Try different methods to get content
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        // For paragraphs, check blocks
        if (block.getBlocks() != null && !block.getBlocks().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (StructuralNode child : block.getBlocks()) {
                if (child.getContent() != null) {
                    sb.append(child.getContent()).append("\n");
                }
            }
            return sb.toString();
        }
        
        return "";
    }
    
    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // Split by newlines and count non-empty lines
        String[] lines = content.split("\n");
        int count = 0;
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        
        return count;
    }
    
    private void validateLineCount(int actualLines, 
                                 com.example.linter.config.rule.LineConfig lineConfig,
                                 ParagraphBlock blockConfig,
                                 BlockValidationContext context,
                                 StructuralNode block,
                                 List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = lineConfig.severity() != null ? lineConfig.severity() : blockConfig.getSeverity();
        
        if (lineConfig.min() != null && actualLines < lineConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.lines.min")
                .location(context.createLocation(block))
                .message("Paragraph has too few lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At least " + lineConfig.min() + " lines")
                .build());
        }
        
        if (lineConfig.max() != null && actualLines > lineConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("paragraph.lines.max")
                .location(context.createLocation(block))
                .message("Paragraph has too many lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At most " + lineConfig.max() + " lines")
                .build());
        }
    }
}