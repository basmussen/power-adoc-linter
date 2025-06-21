package com.example.linter.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

@JsonFormat(shape = JsonFormat.Shape.STRING)
public enum Severity {
    @JsonProperty("error")
    ERROR("error"),
    
    @JsonProperty("warn")
    WARN("warn"),
    
    @JsonProperty("info")
    INFO("info");
    
    private final String value;
    
    Severity(String value) {
        this.value = value;
    }
    
    @JsonValue
    public String getValue() {
        return value;
    }
    
    @JsonCreator
    public static Severity fromString(String value) {
        if (value == null) return null;
        return switch (value.toLowerCase()) {
            case "error" -> ERROR;
            case "warn", "warning" -> WARN;
            case "info" -> INFO;
            default -> throw new IllegalArgumentException("Unknown severity: " + value);
        };
    }
}