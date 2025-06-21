package com.example.linter.validator.block;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;

/**
 * Detects the type of AsciidoctorJ blocks and maps them to our BlockType enum.
 */
public final class BlockTypeDetector {
    
    /**
     * Detects the block type from an AsciidoctorJ StructuralNode.
     * 
     * @param node the node to analyze
     * @return the detected block type, or null if type cannot be determined
     */
    public BlockType detectType(StructuralNode node) {
        if (node == null) {
            return null;
        }
        
        try {
            // Check node context
            String context = node.getContext();
            if (context == null) {
                return null;
            }
        
        // Map AsciidoctorJ contexts to our BlockTypes
        switch (context) {
            case "paragraph":
                return BlockType.PARAGRAPH;
                
            case "listing":
            case "literal":
                return BlockType.LISTING;
                
            case "table":
                return BlockType.TABLE;
                
            case "image":
                return BlockType.IMAGE;
                
            case "verse":
            case "quote":
                return detectVerseOrQuote(node);
                
            case "pass":
                return BlockType.PASS;
                
            case "example":
            case "sidebar":
            case "open":
                // These could contain other blocks, check content
                return detectFromContent(node);
                
            default:
                return null;
        }
        } catch (Exception e) {
            // Handle any exceptions gracefully
            return null;
        }
    }
    
    /**
     * Determines if a quote/verse block is actually a verse block.
     */
    private BlockType detectVerseOrQuote(StructuralNode node) {
        // Verse blocks typically have attribution or cite attributes
        if (node.getAttribute("attribution") != null || 
            node.getAttribute("citetitle") != null ||
            "verse".equals(node.getStyle())) {
            return BlockType.VERSE;
        }
        
        // Could be a quote block containing other content
        return null;
    }
    
    /**
     * Attempts to detect block type from content for container blocks.
     */
    private BlockType detectFromContent(StructuralNode node) {
        // For container blocks, we might need to look at style or role
        String style = node.getStyle();
        if (style != null) {
            switch (style) {
                case "source":
                case "listing":
                    return BlockType.LISTING;
                case "verse":
                    return BlockType.VERSE;
            }
        }
        
        // Check if it's an image block by role
        if (node.hasRole("image") || "image".equals(node.getNodeName())) {
            return BlockType.IMAGE;
        }
        
        // Default to null for unknown container blocks
        return null;
    }
    
    /**
     * Checks if a node is a specific block type.
     */
    public boolean isBlockType(StructuralNode node, BlockType type) {
        return type == detectType(node);
    }
}