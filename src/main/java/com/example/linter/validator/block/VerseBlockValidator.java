package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.blocks.VerseBlock;
import com.example.linter.config.BlockType;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for verse/quote blocks.
 */
public final class VerseBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.VERSE;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          AbstractBlock config,
                                          BlockValidationContext context) {
        
        VerseBlock verseConfig = (VerseBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get verse attributes
        String author = getAuthor(block);
        String source = getSource(block);
        String content = getBlockContent(block);
        
        // Validate author
        if (verseConfig.author() != null) {
            validateAuthor(author, verseConfig, context, block, messages);
        }
        
        // Validate source
        if (verseConfig.source() != null) {
            validateSource(source, verseConfig, context, block, messages);
        }
        
        // Validate lines
        if (verseConfig.lines() != null) {
            validateLines(content, verseConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getAuthor(StructuralNode block) {
        // Author can be in attribution attribute
        Object attr = block.getAttribute("attribution");
        if (attr != null) {
            return attr.toString();
        }
        
        // Or in author attribute
        attr = block.getAttribute("author");
        if (attr != null) {
            return attr.toString();
        }
        
        return null;
    }
    
    private String getSource(StructuralNode block) {
        // Source can be in citetitle attribute
        Object cite = block.getAttribute("citetitle");
        if (cite != null) {
            return cite.toString();
        }
        
        // Or in source attribute
        cite = block.getAttribute("source");
        if (cite != null) {
            return cite.toString();
        }
        
        return null;
    }
    
    private String getBlockContent(StructuralNode block) {
        // Try different methods to get content
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        // For verses, source lines might contain the content
        List<String> lines = block.getSourceLines();
        if (lines != null && !lines.isEmpty()) {
            return String.join("\n", lines);
        }
        
        return "";
    }
    
    private void validateAuthor(String author, VerseBlock config,
                              BlockValidationContext context,
                              StructuralNode block,
                              List<ValidationMessage> messages) {
        
        VerseBlock.AuthorConfig authorConfig = config.author();
        
        // Check if author is required
        if (authorConfig.required() && (author == null || author.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(authorConfig.severity())
                .ruleId("verse.author.required")
                .location(context.createLocation(block))
                .message("Verse block must have an author")
                .actualValue("No author")
                .expectedValue("Author required")
                .build());
            return;
        }
        
        if (author != null && !author.trim().isEmpty()) {
            // Validate author pattern
            if (authorConfig.pattern() != null) {
                Pattern pattern = Pattern.compile(authorConfig.pattern());
                if (!pattern.matcher(author).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(authorConfig.severity())
                        .ruleId("verse.author.pattern")
                        .location(context.createLocation(block))
                        .message("Verse author does not match required pattern")
                        .actualValue(author)
                        .expectedValue("Pattern: " + authorConfig.pattern())
                        .build());
                }
            }
            
            // Validate allowed authors
            if (authorConfig.allowed() != null && !authorConfig.allowed().isEmpty()) {
                if (!authorConfig.allowed().contains(author)) {
                    messages.add(ValidationMessage.builder()
                        .severity(authorConfig.severity())
                        .ruleId("verse.author.allowed")
                        .location(context.createLocation(block))
                        .message("Verse has unauthorized author")
                        .actualValue(author)
                        .expectedValue("One of: " + String.join(", ", authorConfig.allowed()))
                        .build());
                }
            }
        }
    }
    
    private void validateSource(String source, VerseBlock config,
                              BlockValidationContext context,
                              StructuralNode block,
                              List<ValidationMessage> messages) {
        
        VerseBlock.SourceConfig sourceConfig = config.source();
        
        // Check if source is required
        if (sourceConfig.required() && (source == null || source.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(sourceConfig.severity())
                .ruleId("verse.source.required")
                .location(context.createLocation(block))
                .message("Verse block must have a source")
                .actualValue("No source")
                .expectedValue("Source required")
                .build());
            return;
        }
        
        if (source != null && !source.trim().isEmpty()) {
            // Validate source pattern
            if (sourceConfig.pattern() != null) {
                Pattern pattern = Pattern.compile(sourceConfig.pattern());
                if (!pattern.matcher(source).matches()) {
                    messages.add(ValidationMessage.builder()
                        .severity(sourceConfig.severity())
                        .ruleId("verse.source.pattern")
                        .location(context.createLocation(block))
                        .message("Verse source does not match required pattern")
                        .actualValue(source)
                        .expectedValue("Pattern: " + sourceConfig.pattern())
                        .build());
                }
            }
            
            // Validate source format
            if (sourceConfig.format() != null) {
                validateSourceFormat(source, sourceConfig, context, block, messages);
            }
        }
    }
    
    private void validateSourceFormat(String source, VerseBlock.SourceConfig config,
                                    BlockValidationContext context,
                                    StructuralNode block,
                                    List<ValidationMessage> messages) {
        
        String expectedFormat = config.format();
        boolean isValid = false;
        
        switch (expectedFormat) {
            case "book":
                // Check if source looks like a book citation (Title, Year)
                isValid = source.matches(".*,\\s*\\d{4}.*");
                break;
            case "article":
                // Check if source looks like an article citation
                isValid = source.contains("\"") || source.contains("\u201C");
                break;
            case "url":
                // Check if source is a URL
                isValid = source.startsWith("http://") || source.startsWith("https://");
                break;
            case "custom":
                // Custom format, skip validation
                isValid = true;
                break;
        }
        
        if (!isValid) {
            messages.add(ValidationMessage.builder()
                .severity(config.severity())
                .ruleId("verse.source.format")
                .location(context.createLocation(block))
                .message("Verse source does not match expected format")
                .actualValue(source)
                .expectedValue("Format: " + expectedFormat)
                .build());
        }
    }
    
    private void validateLines(String content, VerseBlock config,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        VerseBlock.LineConfig lineConfig = config.lines();
        
        // Count non-empty lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (lineConfig.min() != null && lineCount < lineConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("verse.lines.min")
                .location(context.createLocation(block))
                .message("Verse block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + lineConfig.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (lineConfig.max() != null && lineCount > lineConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("verse.lines.max")
                .location(context.createLocation(block))
                .message("Verse block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + lineConfig.max() + " lines")
                .build());
        }
        
        // Validate line pattern if specified
        if (lineConfig.pattern() != null) {
            validateLinePattern(content, lineConfig, context, block, messages);
        }
    }
    
    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        String[] lines = content.split("\n");
        int count = 0;
        
        // Count non-empty lines
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                count++;
            }
        }
        
        return count;
    }
    
    private void validateLinePattern(String content, VerseBlock.LineConfig config,
                                   BlockValidationContext context,
                                   StructuralNode block,
                                   List<ValidationMessage> messages) {
        
        Pattern pattern = Pattern.compile(config.pattern());
        String[] lines = content.split("\n");
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !pattern.matcher(line).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(config.severity())
                    .ruleId("verse.lines.pattern")
                    .location(context.createLocation(block))
                    .message("Verse line " + (i + 1) + " does not match required pattern")
                    .actualValue(line)
                    .expectedValue("Pattern: " + config.pattern())
                    .build());
            }
        }
    }
}