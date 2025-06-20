package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.BlockType;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for paragraph blocks.
 */
public final class ParagraphBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.PARAGRAPH;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          AbstractBlock config,
                                          BlockValidationContext context) {
        
        ParagraphBlock paragraphConfig = (ParagraphBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get paragraph content
        String content = getBlockContent(block);
        
        // Validate line count if configured
        if (paragraphConfig.lines() != null) {
            int lineCount = countLines(content);
            validateLineCount(lineCount, paragraphConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getBlockContent(StructuralNode block) {
        // Try different methods to get content
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        // For paragraphs, source might contain the content
        List<String> lines = block.getSourceLines();
        if (lines != null && !lines.isEmpty()) {
            return String.join("\n", lines);
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
                                 ParagraphBlock config,
                                 BlockValidationContext context,
                                 StructuralNode block,
                                 List<ValidationMessage> messages) {
        
        ParagraphBlock.LineConfig lineConfig = config.lines();
        
        if (lineConfig.min() != null && actualLines < lineConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("paragraph.lines.min")
                .location(context.createLocation(block))
                .message("Paragraph has too few lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At least " + lineConfig.min() + " lines")
                .build());
        }
        
        if (lineConfig.max() != null && actualLines > lineConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("paragraph.lines.max")
                .location(context.createLocation(block))
                .message("Paragraph has too many lines")
                .actualValue(String.valueOf(actualLines))
                .expectedValue("At most " + lineConfig.max() + " lines")
                .build());
        }
    }
}