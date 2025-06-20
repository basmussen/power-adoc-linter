package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.BlockType;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Validator for listing/code blocks.
 */
public final class ListingBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.LISTING;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          AbstractBlock config,
                                          BlockValidationContext context) {
        
        ListingBlock listingConfig = (ListingBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get listing attributes
        String language = getLanguage(block);
        String title = block.getTitle();
        String content = getBlockContent(block);
        
        // Validate language
        if (listingConfig.getLanguage() != null) {
            validateLanguage(language, listingConfig.getLanguage(), context, block, messages);
        }
        
        // Validate title
        if (listingConfig.getTitle() != null) {
            validateTitle(title, listingConfig.getTitle(), context, block, messages);
        }
        
        // Validate lines
        if (listingConfig.getLines() != null) {
            validateLines(content, listingConfig.getLines(), context, block, messages);
        }
        
        // Validate callouts
        if (listingConfig.getCallouts() != null) {
            validateCallouts(content, listingConfig.getCallouts(), context, block, messages);
        }
        
        return messages;
    }
    
    private String getLanguage(StructuralNode block) {
        // Language can be in different attributes
        Object lang = block.getAttribute("language");
        if (lang != null) {
            return lang.toString();
        }
        
        // Try source attribute
        lang = block.getAttribute("source");
        if (lang != null) {
            return lang.toString();
        }
        
        // Check style for source blocks
        String style = block.getStyle();
        if (style != null && !"source".equals(style)) {
            return style;
        }
        
        return null;
    }
    
    private String getBlockContent(StructuralNode block) {
        // Try different methods to get content
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        // For listings, check blocks
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
    
    private void validateLanguage(String language, ListingBlock.LanguageConfig config,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        // Check if language is required
        if (config.isRequired() && (language == null || language.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("listing.language.required")
                .location(context.createLocation(block))
                .message("Listing block must specify a language")
                .actualValue("No language")
                .expectedValue("Language required")
                .build());
        }
        
        // Validate allowed languages if specified
        if (language != null && config.getAllowed() != null && !config.getAllowed().isEmpty()) {
            if (!config.getAllowed().contains(language)) {
                messages.add(ValidationMessage.builder()
                    .severity(config.getSeverity())
                    .ruleId("listing.language.allowed")
                    .location(context.createLocation(block))
                    .message("Listing block has unsupported language")
                    .actualValue(language)
                    .expectedValue("One of: " + String.join(", ", config.getAllowed()))
                    .build());
            }
        }
    }
    
    private void validateTitle(String title, ListingBlock.TitleConfig config,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("listing.title.required")
                .location(context.createLocation(block))
                .message("Listing block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
        }
    }
    
    private void validateLines(String content, com.example.linter.config.rule.LineConfig config,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (config.min() != null && lineCount < config.min()) {
            messages.add(ValidationMessage.builder()
                .severity(config.severity())
                .ruleId("listing.lines.min")
                .location(context.createLocation(block))
                .message("Listing block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + config.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.max() != null && lineCount > config.max()) {
            messages.add(ValidationMessage.builder()
                .severity(config.severity())
                .ruleId("listing.lines.max")
                .location(context.createLocation(block))
                .message("Listing block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + config.max() + " lines")
                .build());
        }
    }
    
    private void validateCallouts(String content, ListingBlock.CalloutsConfig config,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        // Count callouts in content
        int calloutCount = countCallouts(content);
        
        // Check if callouts are allowed
        if (!config.isAllowed() && calloutCount > 0) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("listing.callouts.notAllowed")
                .location(context.createLocation(block))
                .message("Listing block must not contain callouts")
                .actualValue(calloutCount + " callouts")
                .expectedValue("No callouts allowed")
                .build());
        }
        
        // Validate max callouts
        if (config.getMax() != null && calloutCount > config.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(config.getSeverity())
                .ruleId("listing.callouts.max")
                .location(context.createLocation(block))
                .message("Listing block has too many callouts")
                .actualValue(String.valueOf(calloutCount))
                .expectedValue("At most " + config.getMax() + " callouts")
                .build());
        }
    }
    
    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        String[] lines = content.split("\n");
        return lines.length;
    }
    
    private int countCallouts(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // Count callouts in format <1>, <2>, etc.
        int count = 0;
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            // Simple pattern to find <number>
            if (line.matches(".*<\\d+>.*")) {
                count++;
            }
        }
        
        return count;
    }
}