package com.example.linter.validator.block;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.QuoteBlock;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for quote blocks.
 * Based on the YAML schema structure for validating AsciiDoc quote blocks.
 */
public class QuoteBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.QUOTE;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode node, Block blockConfig, BlockValidationContext context) {
        List<ValidationMessage> results = new ArrayList<>();
        
        if (!(blockConfig instanceof QuoteBlock quoteBlock)) {
            return results;
        }
        
        // Validate author
        if (quoteBlock.getAuthor() != null) {
            validateAuthor(node, quoteBlock.getAuthor(), quoteBlock.getSeverity(), results, context);
        }
        
        // Validate source
        if (quoteBlock.getSource() != null) {
            validateSource(node, quoteBlock.getSource(), quoteBlock.getSeverity(), results, context);
        }
        
        // Validate content
        if (quoteBlock.getContent() != null) {
            validateContent(node, quoteBlock.getContent(), quoteBlock.getSeverity(), results, context);
        }
        
        return results;
    }
    
    private void validateAuthor(StructuralNode node, QuoteBlock.AuthorConfig config, 
                               Severity blockSeverity, List<ValidationMessage> results,
                               BlockValidationContext context) {
        String author = extractAuthor(node);
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.isRequired() && (author == null || author.trim().isEmpty())) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.author.required")
                .message("Quote block requires an author")
                .location(context.createLocation(node))
                .build());
            return;
        }
        
        if (author != null && !author.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && author.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.author.minLength")
                    .message(String.format("Quote author is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), author.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && author.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.author.maxLength")
                    .message(String.format("Quote author is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), author.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(author).matches()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.author.pattern")
                    .message(String.format("Quote author does not match required pattern: %s (actual: %s)",
                                  config.getPattern().pattern(), author))
                    .location(context.createLocation(node))
                    .build());
            }
        }
    }
    
    private void validateSource(StructuralNode node, QuoteBlock.SourceConfig config,
                               Severity blockSeverity, List<ValidationMessage> results,
                               BlockValidationContext context) {
        String source = extractSource(node);
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.isRequired() && (source == null || source.trim().isEmpty())) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.source.required")
                .message("Quote block requires a source")
                .location(context.createLocation(node))
                .build());
            return;
        }
        
        if (source != null && !source.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && source.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.source.minLength")
                    .message(String.format("Quote source is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), source.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && source.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.source.maxLength")
                    .message(String.format("Quote source is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), source.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(source).matches()) {
                results.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("quote.source.pattern")
                    .message(String.format("Quote source does not match required pattern: %s (actual: %s)",
                                  config.getPattern().pattern(), source))
                    .location(context.createLocation(node))
                    .build());
            }
        }
    }
    
    private void validateContent(StructuralNode node, QuoteBlock.ContentConfig config,
                                Severity blockSeverity, List<ValidationMessage> results,
                                BlockValidationContext context) {
        String content = extractContent(node);
        
        if (config.isRequired() && (content == null || content.trim().isEmpty())) {
            results.add(ValidationMessage.builder()
                .severity(blockSeverity)
                .ruleId("quote.content.required")
                .message("Quote block requires content")
                .location(context.createLocation(node))
                .build());
            return;
        }
        
        if (content != null && !content.trim().isEmpty()) {
            // Validate minLength
            if (config.getMinLength() != null && content.length() < config.getMinLength()) {
                results.add(ValidationMessage.builder()
                    .severity(blockSeverity)
                    .ruleId("quote.content.minLength")
                    .message(String.format("Quote content is too short (minimum %d characters, found %d)",
                                  config.getMinLength(), content.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate maxLength
            if (config.getMaxLength() != null && content.length() > config.getMaxLength()) {
                results.add(ValidationMessage.builder()
                    .severity(blockSeverity)
                    .ruleId("quote.content.maxLength")
                    .message(String.format("Quote content is too long (maximum %d characters, found %d)",
                                  config.getMaxLength(), content.length()))
                    .location(context.createLocation(node))
                    .build());
            }
            
            // Validate lines
            if (config.getLines() != null) {
                validateLines(node, content, config.getLines(), blockSeverity, results, context);
            }
        }
    }
    
    private void validateLines(StructuralNode node, String content, QuoteBlock.LinesConfig config,
                              Severity blockSeverity, List<ValidationMessage> results,
                              BlockValidationContext context) {
        String[] lines = content.split("\n");
        int lineCount = lines.length;
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockSeverity;
        
        if (config.getMin() != null && lineCount < config.getMin()) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.content.lines.min")
                .message(String.format("Quote content has too few lines (minimum %d, found %d)",
                              config.getMin(), lineCount))
                .location(context.createLocation(node))
                .build());
        }
        
        if (config.getMax() != null && lineCount > config.getMax()) {
            results.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("quote.content.lines.max")
                .message(String.format("Quote content has too many lines (maximum %d, found %d)",
                              config.getMax(), lineCount))
                .location(context.createLocation(node))
                .build());
        }
    }
    
    private String extractAuthor(StructuralNode node) {
        // Check for author attribute (standard way)
        Object author = node.getAttribute("author");
        if (author != null) {
            return author.toString();
        }
        
        // Check for attribution (alternative way)
        Object attribution = node.getAttribute("attribution");
        if (attribution != null) {
            return attribution.toString();
        }
        
        // Check for positional attribute [quote, Author, Source]
        Object attr1 = node.getAttribute("1");
        if (attr1 != null) {
            return attr1.toString();
        }
        
        return null;
    }
    
    private String extractSource(StructuralNode node) {
        // Check for citetitle attribute (standard way for source)
        Object citetitle = node.getAttribute("citetitle");
        if (citetitle != null) {
            return citetitle.toString();
        }
        
        // Check for source attribute (alternative way)
        Object source = node.getAttribute("source");
        if (source != null) {
            return source.toString();
        }
        
        // Check for positional attribute [quote, Author, Source]
        Object attr2 = node.getAttribute("2");
        if (attr2 != null) {
            return attr2.toString();
        }
        
        return null;
    }
    
    private String extractContent(StructuralNode node) {
        // Try to get content as string
        if (node.getContent() instanceof String) {
            return (String) node.getContent();
        }
        
        // Try to get source (raw content)
        if (node instanceof org.asciidoctor.ast.Block) {
            List<String> lines = ((org.asciidoctor.ast.Block) node).getLines();
            if (lines != null && !lines.isEmpty()) {
                return String.join("\n", lines);
            }
        }
        
        // Fallback to content object toString
        if (node.getContent() != null) {
            return node.getContent().toString();
        }
        
        return null;
    }
}