package com.example.linter.config.loader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.example.linter.config.blocks.*;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Custom deserializer for lists of Block objects.
 * Handles the YAML structure where block type is used as a key.
 * 
 * Example YAML:
 * allowedBlocks:
 *   - paragraph:
 *       name: "intro"
 *       severity: error
 *   - listing:
 *       name: "code"
 */
public class BlockListDeserializer extends JsonDeserializer<List<Block>> {
    
    @Override
    public List<Block> deserialize(JsonParser p, DeserializationContext ctxt) 
            throws IOException, JsonProcessingException {
        
        List<Block> blocks = new ArrayList<>();
        JsonNode node = p.getCodec().readTree(p);
        ObjectMapper mapper = (ObjectMapper) p.getCodec();
        
        if (node.isArray()) {
            for (JsonNode item : node) {
                if (item.isObject()) {
                    // The item should have exactly one field, where the field name is the block type
                    Iterator<Map.Entry<String, JsonNode>> fields = item.fields();
                    if (fields.hasNext()) {
                        Map.Entry<String, JsonNode> entry = fields.next();
                        String blockType = entry.getKey();
                        JsonNode blockData = entry.getValue();
                        
                        Block block = deserializeBlock(blockType, blockData, mapper);
                        if (block != null) {
                            blocks.add(block);
                        }
                    }
                }
            }
        }
        
        return blocks;
    }
    
    private Block deserializeBlock(String type, JsonNode data, ObjectMapper mapper) 
            throws JsonProcessingException {
        
        Class<? extends Block> blockClass = getBlockClass(type);
        if (blockClass == null) {
            throw new IllegalArgumentException("Unknown block type: " + type);
        }
        
        return mapper.treeToValue(data, blockClass);
    }
    
    private Class<? extends Block> getBlockClass(String type) {
        return switch (type.toLowerCase()) {
            case "paragraph" -> ParagraphBlock.class;
            case "listing" -> ListingBlock.class;
            case "table" -> TableBlock.class;
            case "image" -> ImageBlock.class;
            case "verse" -> VerseBlock.class;
            default -> null;
        };
    }
}