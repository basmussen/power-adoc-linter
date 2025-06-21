package com.example.linter.validator.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.AdmonitionBlock;
import com.example.linter.config.blocks.Block;
import com.example.linter.validator.ValidationMessage;

/**
 * Validator for admonition blocks in AsciiDoc documents.
 * 
 * <p>This validator validates admonition blocks (NOTE, TIP, IMPORTANT, WARNING, CAUTION)
 * based on the YAML schema structure defined in 
 * {@code src/main/resources/schemas/blocks/admonition-block.yaml}.
 * The YAML configuration is parsed into {@link AdmonitionBlock} objects which
 * define the validation rules.</p>
 * 
 * <p>Supported validation rules from YAML schema:</p>
 * <ul>
 *   <li><b>title</b>: Validates block title (required, pattern, min/max length)</li>
 *   <li><b>content</b>: Validates content length (min/max characters)</li>
 *   <li><b>lines</b>: Validates line count (min/max)</li>
 *   <li><b>icon</b>: Validates icon settings (enabled/disabled)</li>
 *   <li><b>typeOccurrences</b>: Validates max occurrences per admonition type</li>
 * </ul>
 * 
 * <p>Each nested configuration can optionally define its own severity level.
 * If not specified, the block-level severity is used as fallback.</p>
 * 
 * @see AdmonitionBlock
 * @see BlockTypeValidator
 */
public final class AdmonitionBlockValidator implements BlockTypeValidator {
    
    private final Map<String, Integer> typeOccurrences = new HashMap<>();
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.ADMONITION;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, 
                                          Block config,
                                          BlockValidationContext context) {
        
        AdmonitionBlock admonitionConfig = (AdmonitionBlock) config;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Get admonition attributes
        String admonitionType = getAdmonitionType(block);
        String title = block.getTitle();
        String content = getBlockContent(block);
        boolean hasIcon = hasIcon(block);
        
        // Track occurrences
        if (admonitionType != null) {
            int count = typeOccurrences.getOrDefault(admonitionType, 0) + 1;
            typeOccurrences.put(admonitionType, count);
        }
        
        // Validate title
        if (admonitionConfig.getTitle() != null) {
            validateTitle(title, admonitionConfig.getTitle(), admonitionConfig, context, block, messages);
        }
        
        // Validate content
        if (admonitionConfig.getContent() != null) {
            validateContent(content, admonitionConfig.getContent(), admonitionConfig, context, block, messages);
        }
        
        // Validate lines
        if (admonitionConfig.getLines() != null) {
            validateLines(content, admonitionConfig.getLines(), admonitionConfig, context, block, messages);
        }
        
        // Validate icon
        if (admonitionConfig.getIcon() != null) {
            validateIcon(hasIcon, admonitionConfig.getIcon(), admonitionConfig, context, block, messages);
        }
        
        // Validate type occurrences
        if (admonitionConfig.getTypeOccurrences() != null && admonitionType != null) {
            validateTypeOccurrences(admonitionType, admonitionConfig.getTypeOccurrences(), 
                                  admonitionConfig, context, block, messages);
        }
        
        return messages;
    }
    
    private String getAdmonitionType(StructuralNode block) {
        // Admonition type is typically in the style attribute
        String style = block.getStyle();
        if (style != null) {
            // Convert to uppercase for consistency
            return style.toUpperCase();
        }
        
        // Try role attribute as fallback
        Object role = block.getAttribute("role");
        if (role != null) {
            return role.toString().toUpperCase();
        }
        
        return null;
    }
    
    private String getBlockContent(StructuralNode block) {
        // Try different methods to get content
        if (block.getContent() != null) {
            return block.getContent().toString();
        }
        
        // For admonitions, check blocks
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
    
    private boolean hasIcon(StructuralNode block) {
        // Check if icons are enabled at document level
        Object docIcons = block.getDocument().getAttribute("icons");
        if (docIcons != null && "font".equals(docIcons.toString())) {
            return true;
        }
        
        // Check block-level icon attribute
        Object blockIcon = block.getAttribute("icon");
        return blockIcon != null && !"none".equals(blockIcon.toString());
    }
    
    private void validateTitle(String title, AdmonitionBlock.TitleConfig config,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (title == null || title.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.title.required")
                .location(context.createLocation(block))
                .message("Admonition block must have a title")
                .actualValue("No title")
                .expectedValue("Title required")
                .build());
            return;
        }
        
        if (title != null) {
            // Validate pattern
            if (config.getPattern() != null && !config.getPattern().matcher(title).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.pattern")
                    .location(context.createLocation(block))
                    .message("Admonition title does not match required pattern")
                    .actualValue(title)
                    .expectedValue("Pattern: " + config.getPattern().pattern())
                    .build());
            }
            
            // Validate min length
            if (config.getMinLength() != null && title.length() < config.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.minLength")
                    .location(context.createLocation(block))
                    .message("Admonition title is too short")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At least " + config.getMinLength() + " characters")
                    .build());
            }
            
            // Validate max length
            if (config.getMaxLength() != null && title.length() > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("admonition.title.maxLength")
                    .location(context.createLocation(block))
                    .message("Admonition title is too long")
                    .actualValue(title.length() + " characters")
                    .expectedValue("At most " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private void validateContent(String content, AdmonitionBlock.ContentConfig config,
                               AdmonitionBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        int contentLength = content.trim().length();
        
        // Validate min length
        if (config.getMinLength() != null && contentLength < config.getMinLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.minLength")
                .location(context.createLocation(block))
                .message("Admonition content is too short")
                .actualValue(contentLength + " characters")
                .expectedValue("At least " + config.getMinLength() + " characters")
                .build());
        }
        
        // Validate max length
        if (config.getMaxLength() != null && contentLength > config.getMaxLength()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.content.maxLength")
                .location(context.createLocation(block))
                .message("Admonition content is too long")
                .actualValue(contentLength + " characters")
                .expectedValue("At most " + config.getMaxLength() + " characters")
                .build());
        }
    }
    
    private void validateLines(String content, com.example.linter.config.rule.LineConfig config,
                             AdmonitionBlock blockConfig,
                             BlockValidationContext context,
                             StructuralNode block,
                             List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.severity() != null ? config.severity() : blockConfig.getSeverity();
        
        // Count lines
        int lineCount = countLines(content);
        
        // Validate min lines
        if (config.min() != null && lineCount < config.min()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.lines.min")
                .location(context.createLocation(block))
                .message("Admonition block has too few lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At least " + config.min() + " lines")
                .build());
        }
        
        // Validate max lines
        if (config.max() != null && lineCount > config.max()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.lines.max")
                .location(context.createLocation(block))
                .message("Admonition block has too many lines")
                .actualValue(String.valueOf(lineCount))
                .expectedValue("At most " + config.max() + " lines")
                .build());
        }
    }
    
    private void validateIcon(boolean hasIcon, AdmonitionBlock.IconConfig config,
                            AdmonitionBlock blockConfig,
                            BlockValidationContext context,
                            StructuralNode block,
                            List<ValidationMessage> messages) {
        
        // Get severity with fallback to block severity
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isEnabled() && !hasIcon) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.icon.required")
                .location(context.createLocation(block))
                .message("Admonition block must have an icon")
                .actualValue("No icon")
                .expectedValue("Icon required")
                .build());
        } else if (!config.isEnabled() && hasIcon) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.icon.notAllowed")
                .location(context.createLocation(block))
                .message("Admonition block must not have an icon")
                .actualValue("Icon present")
                .expectedValue("No icon allowed")
                .build());
        }
    }
    
    private void validateTypeOccurrences(String admonitionType, 
                                       Map<String, AdmonitionBlock.TypeOccurrenceConfig> config,
                                       AdmonitionBlock blockConfig,
                                       BlockValidationContext context,
                                       StructuralNode block,
                                       List<ValidationMessage> messages) {
        
        AdmonitionBlock.TypeOccurrenceConfig typeConfig = config.get(admonitionType);
        if (typeConfig == null) {
            return;
        }
        
        // Get severity with fallback to block severity
        Severity severity = typeConfig.getSeverity() != null ? typeConfig.getSeverity() : blockConfig.getSeverity();
        
        int currentCount = typeOccurrences.getOrDefault(admonitionType, 0);
        
        if (typeConfig.getMax() != null && currentCount > typeConfig.getMax()) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .ruleId("admonition.typeOccurrences.max")
                .location(context.createLocation(block))
                .message("Too many " + admonitionType + " admonition blocks")
                .actualValue(String.valueOf(currentCount))
                .expectedValue("At most " + typeConfig.getMax())
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
}