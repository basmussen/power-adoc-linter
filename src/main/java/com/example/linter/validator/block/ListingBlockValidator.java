package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.BlockType;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        if (listingConfig.language() != null) {
            validateLanguage(language, listingConfig, context, block, messages);
        }
        
        // Validate title
        if (listingConfig.title() != null) {
            validateTitle(title, listingConfig, context, block, messages);
        }
        
        // Validate callouts
        if (listingConfig.callouts() != null) {
            validateCallouts(content, listingConfig, context, block, messages);
        }
        
        // Validate lines
        if (listingConfig.lines() != null) {
            validateLines(content, listingConfig, context, block, messages);
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
        
        // For listings, source lines contain the content
        List<String> lines = block.getSourceLines();
        if (lines != null && !lines.isEmpty()) {
            return String.join("\n", lines);
        }
        
        return "";
    }
    
    private void validateLanguage(String language, ListingBlock config,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        ListingBlock.LanguageConfig langConfig = config.language();
        
        // Check if language is required
        if (langConfig.required() && (language == null || language.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(langConfig.severity())
                .ruleId("listing.language.required")
                .location(context.createLocation(block))
                .message("Listing block must specify a language")
                .actualValue("No language")
                .expectedValue("Language required")
                .build());
            return;
        }
        
        // Validate allowed languages
        if (language != null && langConfig.allowed() != null && !langConfig.allowed().isEmpty()) {
            if (!langConfig.allowed().contains(language)) {
                messages.add(ValidationMessage.builder()
                    .severity(langConfig.severity())
                    .ruleId("listing.language.allowed")
                    .location(context.createLocation(block))
                    .message("Listing block has unsupported language")
                    .actualValue(language)
                    .expectedValue("One of: " + String.join(", ", langConfig.allowed()))
                    .build());
            }
        }
        
        // Validate language pattern
        if (language != null && langConfig.pattern() != null) {
            Pattern pattern = Pattern.compile(langConfig.pattern());
            if (!pattern.matcher(language).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(langConfig.severity())
                    .ruleId("listing.language.pattern")
                    .location(context.createLocation(block))
                    .message("Listing language does not match required pattern")
                    .actualValue(language)
                    .expectedValue("Pattern: " + langConfig.pattern())
                    .build());
            }
        }
    }
    
    private void validateTitle(String title, ListingBlock config,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        ListingBlock.TitleConfig titleConfig = config.title();
        
        if (titleConfig.required() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(titleConfig.severity())
                .ruleId("listing.title.required")
                .location(context.createLocation(block))
                .message("Listing block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
        }
    }
    
    private void validateCallouts(String content, ListingBlock config,
                                BlockValidationContext context,
                                StructuralNode block,
                                List<ValidationMessage> messages) {
        
        ListingBlock.CalloutConfig calloutConfig = config.callouts();
        
        // Count callouts in content
        int calloutCount = countCallouts(content);
        
        // Check if callouts are required
        if (calloutConfig.required() && calloutCount == 0) {
            messages.add(ValidationMessage.builder()
                .severity(calloutConfig.severity())
                .ruleId("listing.callouts.required")
                .location(context.createLocation(block))
                .message("Listing block must contain callouts")
                .actualValue("No callouts")
                .expectedValue("At least one callout required")
                .build());
            return;
        }
        
        // Validate min/max callouts
        if (calloutConfig.min() != null && calloutCount < calloutConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(calloutConfig.severity())
                .ruleId("listing.callouts.min")
                .location(context.createLocation(block))
                .message("Listing block has too few callouts")
                .actualValue(String.valueOf(calloutCount))
                .expectedValue("At least " + calloutConfig.min() + " callouts")
                .build());
        }
        
        if (calloutConfig.max() != null && calloutCount > calloutConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(calloutConfig.severity())
                .ruleId("listing.callouts.max")
                .location(context.createLocation(block))
                .message("Listing block has too many callouts")
                .actualValue(String.valueOf(calloutCount))
                .expectedValue("At most " + calloutConfig.max() + " callouts")
                .build());
        }
        
        // Validate callout style
        if (calloutConfig.style() != null && calloutCount > 0) {
            validateCalloutStyle(content, calloutConfig, context, block, messages);
        }
    }
    
    private int countCallouts(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        // Count callouts in format <1>, <2>, etc.
        Pattern calloutPattern = Pattern.compile("<\\d+>");
        int count = 0;
        String[] lines = content.split("\n");
        
        for (String line : lines) {
            if (calloutPattern.matcher(line).find()) {
                count++;
            }
        }
        
        return count;
    }
    
    private void validateCalloutStyle(String content, ListingBlock.CalloutConfig config,
                                    BlockValidationContext context,
                                    StructuralNode block,
                                    List<ValidationMessage> messages) {
        
        String expectedStyle = config.style();
        Pattern calloutPattern;
        
        // Define patterns for different callout styles
        switch (expectedStyle) {
            case "angle":
                calloutPattern = Pattern.compile("<\\d+>");
                break;
            case "circle":
                calloutPattern = Pattern.compile("\\(\\d+\\)");
                break;
            case "conum":
                calloutPattern = Pattern.compile("CO\\d+");
                break;
            default:
                // Unknown style, skip validation
                return;
        }
        
        // Check if content uses the expected style
        String[] lines = content.split("\n");
        for (String line : lines) {
            // Check for any callout
            if (line.matches(".*[<(]\\d+[>)].*") || line.contains("CO")) {
                if (!calloutPattern.matcher(line).find()) {
                    messages.add(ValidationMessage.builder()
                        .severity(config.severity())
                        .ruleId("listing.callouts.style")
                        .location(context.createLocation(block))
                        .message("Listing uses incorrect callout style")
                        .actualValue("Mixed or incorrect style")
                        .expectedValue("Style: " + expectedStyle)
                        .build());
                    break;
                }
            }
        }
    }
    
    private void validateLines(String content, ListingBlock config,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        ListingBlock.LineConfig lineConfig = config.lines();
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (lineConfig.min() != null && lineCount < lineConfig.min()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("listing.lines.min")
                .location(context.createLocation(block))
                .message("Listing block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + lineConfig.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (lineConfig.max() != null && lineCount > lineConfig.max()) {
            messages.add(ValidationMessage.builder()
                .severity(lineConfig.severity())
                .ruleId("listing.lines.max")
                .location(context.createLocation(block))
                .message("Listing block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + lineConfig.max() + " lines")
                .build());
        }
        
        // Validate max line length
        if (lineConfig.maxLength() != null) {
            validateLineLength(content, lineConfig, context, block, messages);
        }
    }
    
    private int countLines(String content) {
        if (content == null || content.isEmpty()) {
            return 0;
        }
        
        String[] lines = content.split("\n");
        return lines.length;
    }
    
    private void validateLineLength(String content, ListingBlock.LineConfig config,
                                  BlockValidationContext context,
                                  StructuralNode block,
                                  List<ValidationMessage> messages) {
        
        String[] lines = content.split("\n");
        int maxLength = config.maxLength();
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].length() > maxLength) {
                messages.add(ValidationMessage.builder()
                    .severity(config.severity())
                    .ruleId("listing.lines.maxLength")
                    .location(context.createLocation(block))
                    .message("Listing line " + (i + 1) + " exceeds maximum length")
                    .actualValue(lines[i].length() + " characters")
                    .expectedValue("At most " + maxLength + " characters")
                    .build());
            }
        }
    }
}