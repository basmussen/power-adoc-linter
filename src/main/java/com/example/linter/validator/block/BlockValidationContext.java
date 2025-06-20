package com.example.linter.validator.block;

import com.example.linter.config.blocks.AbstractBlock;
import com.example.linter.validator.SourceLocation;
import org.asciidoctor.ast.Section;
import org.asciidoctor.ast.StructuralNode;

import java.util.*;

/**
 * Context for block validation containing section information and tracking data.
 */
public final class BlockValidationContext {
    private final Section section;
    private final String filename;
    private final Map<String, List<BlockOccurrence>> occurrences;
    private final List<BlockPosition> blockOrder;
    
    public BlockValidationContext(Section section, String filename) {
        this.section = Objects.requireNonNull(section, "section must not be null");
        this.filename = Objects.requireNonNull(filename, "filename must not be null");
        this.occurrences = new HashMap<>();
        this.blockOrder = new ArrayList<>();
    }
    
    public Section getSection() {
        return section;
    }
    
    public String getFilename() {
        return filename;
    }
    
    /**
     * Creates a source location for the given block.
     */
    public SourceLocation createLocation(StructuralNode block) {
        int line = 1;
        if (block.getSourceLocation() != null) {
            line = block.getSourceLocation().getLineNumber();
        }
        
        return SourceLocation.builder()
            .filename(filename)
            .line(line)
            .build();
    }
    
    /**
     * Tracks a block occurrence for validation.
     */
    public void trackBlock(AbstractBlock config, StructuralNode block) {
        String key = createOccurrenceKey(config);
        
        BlockOccurrence occurrence = new BlockOccurrence(config, block, blockOrder.size());
        occurrences.computeIfAbsent(key, k -> new ArrayList<>()).add(occurrence);
        
        blockOrder.add(new BlockPosition(config, block, blockOrder.size()));
    }
    
    /**
     * Gets all occurrences for a specific block configuration.
     */
    public List<BlockOccurrence> getOccurrences(AbstractBlock config) {
        String key = createOccurrenceKey(config);
        return occurrences.getOrDefault(key, Collections.emptyList());
    }
    
    /**
     * Gets the count of occurrences for a specific block configuration.
     */
    public int getOccurrenceCount(AbstractBlock config) {
        return getOccurrences(config).size();
    }
    
    /**
     * Gets all tracked blocks in order.
     */
    public List<BlockPosition> getBlockOrder() {
        return new ArrayList<>(blockOrder);
    }
    
    /**
     * Gets a human-readable name for the block.
     */
    public String getBlockName(AbstractBlock config) {
        if (config.name() != null) {
            return "block '" + config.name() + "'";
        }
        return config.type().toString().toLowerCase() + " block";
    }
    
    private String createOccurrenceKey(AbstractBlock config) {
        if (config.name() != null) {
            return config.type() + ":" + config.name();
        }
        return config.type().toString();
    }
    
    /**
     * Represents a block occurrence with its configuration and position.
     */
    public static final class BlockOccurrence {
        private final AbstractBlock config;
        private final StructuralNode block;
        private final int position;
        
        BlockOccurrence(AbstractBlock config, StructuralNode block, int position) {
            this.config = config;
            this.block = block;
            this.position = position;
        }
        
        public AbstractBlock getConfig() { return config; }
        public StructuralNode getBlock() { return block; }
        public int getPosition() { return position; }
    }
    
    /**
     * Represents a block's position in the document.
     */
    public static final class BlockPosition {
        private final AbstractBlock config;
        private final StructuralNode block;
        private final int index;
        
        BlockPosition(AbstractBlock config, StructuralNode block, int index) {
            this.config = config;
            this.block = block;
            this.index = index;
        }
        
        public AbstractBlock getConfig() { return config; }
        public StructuralNode getBlock() { return block; }
        public int getIndex() { return index; }
    }
}