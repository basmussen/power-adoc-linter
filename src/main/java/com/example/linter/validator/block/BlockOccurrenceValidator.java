package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.validator.ValidationMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Validates block occurrence rules (min/max occurrences).
 */
public final class BlockOccurrenceValidator {
    
    /**
     * Validates occurrence rules for all blocks in a section.
     * 
     * @param context the validation context containing tracked blocks
     * @param blocks the configured blocks with their occurrence rules
     * @return list of validation messages
     */
    public List<ValidationMessage> validate(BlockValidationContext context,
                                          List<AbstractBlock> blocks) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(blocks, "blocks must not be null");
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        for (AbstractBlock block : blocks) {
            if (block.occurrences() != null) {
                validateOccurrences(block, context, messages);
            }
        }
        
        return messages;
    }
    
    /**
     * Validates occurrences for a specific block configuration.
     */
    private void validateOccurrences(AbstractBlock block,
                                   BlockValidationContext context,
                                   List<ValidationMessage> messages) {
        
        AbstractBlock.OccurrenceConfig occurrences = block.occurrences();
        int actualCount = context.getOccurrenceCount(block);
        String blockName = context.getBlockName(block);
        
        // Validate minimum occurrences
        if (occurrences.min() != null && actualCount < occurrences.min()) {
            messages.add(ValidationMessage.builder()
                .severity(occurrences.severity())
                .ruleId("block.occurrences.min")
                .location(createSectionLocation(context))
                .message("Too few occurrences of " + blockName)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At least " + occurrences.min() + " occurrences")
                .build());
        }
        
        // Validate maximum occurrences
        if (occurrences.max() != null && actualCount > occurrences.max()) {
            messages.add(ValidationMessage.builder()
                .severity(occurrences.severity())
                .ruleId("block.occurrences.max")
                .location(createSectionLocation(context))
                .message("Too many occurrences of " + blockName)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("At most " + occurrences.max() + " occurrences")
                .build());
        }
        
        // Validate exact occurrences
        if (occurrences.exact() != null && actualCount != occurrences.exact()) {
            messages.add(ValidationMessage.builder()
                .severity(occurrences.severity())
                .ruleId("block.occurrences.exact")
                .location(createSectionLocation(context))
                .message("Incorrect number of occurrences of " + blockName)
                .actualValue(String.valueOf(actualCount))
                .expectedValue("Exactly " + occurrences.exact() + " occurrences")
                .build());
        }
    }
    
    /**
     * Creates a location for the section.
     */
    private com.example.linter.validator.SourceLocation createSectionLocation(
            BlockValidationContext context) {
        
        return com.example.linter.validator.SourceLocation.builder()
            .filename(context.getFilename())
            .line(1) // Section start
            .build();
    }
}