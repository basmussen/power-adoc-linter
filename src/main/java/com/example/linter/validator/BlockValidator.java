package com.example.linter.validator;

import com.example.linter.config.rule.SectionConfig;
import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.validator.block.*;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Main validator for blocks within sections.
 * Orchestrates block type validation, occurrence validation, and order validation.
 */
public final class BlockValidator {
    
    private final BlockValidatorFactory validatorFactory;
    private final BlockTypeDetector typeDetector;
    private final BlockOccurrenceValidator occurrenceValidator;
    private final BlockOrderValidator orderValidator;
    
    public BlockValidator() {
        this.validatorFactory = new BlockValidatorFactory();
        this.typeDetector = new BlockTypeDetector();
        this.occurrenceValidator = new BlockOccurrenceValidator();
        this.orderValidator = new BlockOrderValidator();
    }
    
    /**
     * Validates all blocks within a section against the configuration.
     * 
     * @param section the AsciiDoc section to validate
     * @param config the section configuration containing block rules
     * @param filename the filename for error reporting
     * @return validation result containing all messages
     */
    public ValidationResult validate(Section section, 
                                   SectionConfig config,
                                   String filename) {
        Objects.requireNonNull(section, "section must not be null");
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(filename, "filename must not be null");
        
        List<ValidationMessage> messages = new ArrayList<>();
        
        // No block validation if no blocks configured
        if (config.allowedBlocks() == null || config.allowedBlocks().isEmpty()) {
            return new ValidationResult(messages);
        }
        
        // Create validation context
        BlockValidationContext context = new BlockValidationContext(section, filename);
        
        // First pass: validate individual blocks and track occurrences
        validateBlocks(section, config, context, messages);
        
        // Second pass: validate occurrences
        messages.addAll(occurrenceValidator.validate(context, config.allowedBlocks()));
        
        // Order validation would go here once OrderConfig is implemented
        
        return new ValidationResult(messages);
    }
    
    /**
     * Validates individual blocks and tracks them in the context.
     */
    private void validateBlocks(Section section,
                              SectionConfig config,
                              BlockValidationContext context,
                              List<ValidationMessage> messages) {
        
        // Get all blocks from the section
        List<StructuralNode> blocks = section.getBlocks();
        
        for (StructuralNode block : blocks) {
            // Detect block type
            BlockType actualType = typeDetector.detectType(block);
            
            if (actualType == null) {
                // Unknown block type, skip
                continue;
            }
            
            // Find matching configuration
            AbstractBlock blockConfig = findBlockConfig(actualType, block, config.allowedBlocks());
            
            if (blockConfig != null) {
                // Track the block
                context.trackBlock(blockConfig, block);
                
                // Validate if we have a validator for this type
                BlockTypeValidator validator = validatorFactory.getValidator(actualType);
                if (validator != null) {
                    messages.addAll(validator.validate(block, blockConfig, context));
                }
            }
        }
    }
    
    /**
     * Finds the configuration for a specific block.
     */
    private AbstractBlock findBlockConfig(BlockType type, 
                                        StructuralNode block,
                                        List<AbstractBlock> configs) {
        // First try to match by name attribute
        Object nameAttr = block.getAttribute("name");
        if (nameAttr != null) {
            String name = nameAttr.toString();
            for (AbstractBlock config : configs) {
                if (config.type() == type && name.equals(config.name())) {
                    return config;
                }
            }
        }
        
        // Then match by type only
        for (AbstractBlock config : configs) {
            if (config.type() == type && config.name() == null) {
                return config;
            }
        }
        
        return null;
    }
}