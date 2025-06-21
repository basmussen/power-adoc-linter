package com.example.linter.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum BlockType {
    @JsonProperty("paragraph")
    PARAGRAPH("paragraph"),
    
    @JsonProperty("listing")
    LISTING("listing"),
    
    @JsonProperty("table")
    TABLE("table"),
    
    @JsonProperty("image")
    IMAGE("image"),
    
    @JsonProperty("verse")
    VERSE("verse");
    
    private final String value;
    
    BlockType(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static BlockType fromString(String value) {
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