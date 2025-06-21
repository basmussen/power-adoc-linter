package com.example.linter.validator.block;

import java.util.List;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;
import com.example.linter.config.blocks.Block;
import com.example.linter.validator.ValidationMessage;

/**
 * Interface for block type specific validators.
 * Each block type (paragraph, table, image, etc.) has its own implementation.
 */
public interface BlockTypeValidator {
    
    /**
     * Returns the block type that this validator supports.
     * 
     * @return the supported block type
     */
    BlockType getSupportedType();
    
    /**
     * Validates a block against its configuration.
     * 
     * @param block the AsciidoctorJ block to validate
     * @param config the block configuration containing validation rules
     * @param context the validation context containing section information
     * @return list of validation messages (errors, warnings, info)
     */
    List<ValidationMessage> validate(StructuralNode block, 
                                   Block config,
                                   BlockValidationContext context);
}