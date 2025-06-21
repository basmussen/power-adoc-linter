package com.example.linter.config.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.linter.config.BlockType;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.ImageBlock;
import com.example.linter.config.blocks.ListingBlock;
import com.example.linter.config.blocks.ParagraphBlock;
import com.example.linter.config.blocks.TableBlock;
import com.example.linter.config.blocks.VerseBlock;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Custom deserializer for Block lists in YAML.
 * Handles the special YAML structure where block type is the key:
 * <pre>
 * allowedBlocks:
 *   - paragraph:
 *       name: intro-paragraph
 *       severity: warn
 *   - listing:
 *       name: code-example
 *       severity: error
 * </pre>
 */
public class BlockListDeserializer extends JsonDeserializer<List<Block>> {
    
    @Override
    public List<Block> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        List<Block> blocks = new ArrayList<>();
        JsonNode node = p.getCodec().readTree(p);
        
        if (!node.isArray()) {
            throw new IOException("Expected array for block list");
        }
        
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        
        for (JsonNode blockNode : node) {
            if (!blockNode.isObject()) {
                continue;
            }
            
            // Each block is an object with a single key (the block type)
            String blockType = blockNode.fieldNames().next();
            JsonNode blockData = blockNode.get(blockType);
            
            // Convert blockType string to BlockType enum
            BlockType type = BlockType.fromValue(blockType);
            
            // Check if min/max are at block level and move them to occurrence
            if ((blockData.has("min") || blockData.has("max")) && !blockData.has("occurrence")) {
                JsonNode minNode = blockData.get("min");
                JsonNode maxNode = blockData.get("max");
                
                // Create occurrence node
                ObjectNode occurrenceNode = mapper.createObjectNode();
                if (minNode != null) {
                    occurrenceNode.set("min", minNode);
                    ((ObjectNode) blockData).remove("min");
                }
                if (maxNode != null) {
                    occurrenceNode.set("max", maxNode);
                    ((ObjectNode) blockData).remove("max");
                }
                
                ((ObjectNode) blockData).set("occurrence", occurrenceNode);
            }
            
            // Ensure severity exists - use WARN as default if missing
            if (!blockData.has("severity")) {
                ((ObjectNode) blockData).put("severity", "warn");
            }
            
            // Deserialize based on block type
            Block block = switch (type) {
                case PARAGRAPH -> mapper.treeToValue(blockData, ParagraphBlock.class);
                case LISTING -> mapper.treeToValue(blockData, ListingBlock.class);
                case TABLE -> mapper.treeToValue(blockData, TableBlock.class);
                case IMAGE -> mapper.treeToValue(blockData, ImageBlock.class);
                case VERSE -> mapper.treeToValue(blockData, VerseBlock.class);
            };
            
            blocks.add(block);
        }
        
        return blocks;
    }
}