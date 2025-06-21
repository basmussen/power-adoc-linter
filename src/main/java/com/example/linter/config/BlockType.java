package com.example.linter.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum BlockType {
    PARAGRAPH,
    LISTING,
    TABLE,
    IMAGE,
    VERSE;
    
    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
    
    @JsonCreator
    public static BlockType fromValue(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "paragraph" -> PARAGRAPH;
            case "listing" -> LISTING;
            case "table" -> TABLE;
            case "image" -> IMAGE;
            case "verse" -> VERSE;
            default -> throw new IllegalArgumentException("Unknown block type: " + value);
        };
    }
}