package com.example.linter.documentation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts regular expression patterns into human-readable descriptions.
 */
public class PatternHumanizer {
    
    private final Map<String, String> knownPatterns;
    
    public PatternHumanizer() {
        this.knownPatterns = new HashMap<>();
        initializeKnownPatterns();
    }
    
    private void initializeKnownPatterns() {
        // Common patterns
        knownPatterns.put("^[A-Z].*", "Muss mit einem Großbuchstaben beginnen");
        knownPatterns.put("^[a-z].*", "Muss mit einem Kleinbuchstaben beginnen");
        knownPatterns.put("^\\d+\\.\\d+\\.\\d+$", "Semantic Versioning Format (z.B. 1.0.0)");
        knownPatterns.put("^[\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$", "Gültige E-Mail-Adresse");
        
        // URL patterns
        knownPatterns.put("^https?://.*", "Muss mit http:// oder https:// beginnen");
        knownPatterns.put(".*\\.(png|jpg|jpeg|gif|svg)$", "Bilddatei (PNG, JPG, JPEG, GIF oder SVG)");
        knownPatterns.put(".*\\.(mp3|ogg|wav|m4a)$", "Audiodatei (MP3, OGG, WAV oder M4A)");
        
        // Title patterns
        knownPatterns.put("^(Introduction|Einführung)$", "Muss 'Introduction' oder 'Einführung' sein");
        knownPatterns.put("^Listing \\d+:.*", "Muss mit 'Listing' gefolgt von einer Nummer und Doppelpunkt beginnen");
        knownPatterns.put("^Table \\d+:.*", "Muss mit 'Table' gefolgt von einer Nummer und Doppelpunkt beginnen");
        knownPatterns.put("^Figure \\d+:.*", "Muss mit 'Figure' gefolgt von einer Nummer und Doppelpunkt beginnen");
        
        // Language patterns
        knownPatterns.put("^(java|python|javascript|yaml|json|xml)$", "Erlaubte Sprachen: java, python, javascript, yaml, json, xml");
    }
    
    /**
     * Converts a regex pattern to a human-readable description.
     * 
     * @param pattern the pattern to humanize
     * @return a human-readable description
     */
    public String humanize(Pattern pattern) {
        if (pattern == null) {
            return "";
        }
        return humanize(pattern.pattern());
    }
    
    /**
     * Converts a regex pattern string to a human-readable description.
     * 
     * @param patternString the pattern string to humanize
     * @return a human-readable description
     */
    public String humanize(String patternString) {
        if (patternString == null || patternString.isEmpty()) {
            return "";
        }
        
        // Check known patterns first
        String known = knownPatterns.get(patternString);
        if (known != null) {
            return known;
        }
        
        // Try to generate description for common patterns
        String description = generateDescription(patternString);
        if (description != null) {
            return description;
        }
        
        // Fallback: show the pattern itself
        return "Muss dem Muster entsprechen: " + patternString;
    }
    
    private String generateDescription(String pattern) {
        // Handle file extensions
        if (pattern.matches(".*\\\\\\.(\\w+\\|)*\\w+\\)\\$")) {
            String extensions = pattern.replaceAll(".*\\\\\\.(\\()?", "")
                                     .replaceAll("\\)\\$", "")
                                     .replaceAll("\\|", ", ");
            return "Dateiendung muss sein: " + extensions.toUpperCase();
        }
        
        // Handle simple starts-with patterns
        if (pattern.startsWith("^") && !pattern.contains("$")) {
            String prefix = pattern.substring(1).replaceAll("\\\\_", "_");
            if (!prefix.contains("[") && !prefix.contains("(")) {
                return "Muss mit '" + prefix + "' beginnen";
            }
        }
        
        // Handle simple ends-with patterns
        if (pattern.endsWith("$") && !pattern.startsWith("^")) {
            String suffix = pattern.substring(0, pattern.length() - 1);
            if (!suffix.contains("[") && !suffix.contains("(")) {
                return "Muss mit '" + suffix + "' enden";
            }
        }
        
        // Handle exact match patterns
        if (pattern.startsWith("^") && pattern.endsWith("$")) {
            String exact = pattern.substring(1, pattern.length() - 1);
            if (!exact.contains("[") && !exact.contains("(") && !exact.contains("*") && !exact.contains("+")) {
                return "Muss genau '" + exact + "' sein";
            }
        }
        
        // Handle character class patterns
        if (pattern.equals("^[A-Za-z]+$")) {
            return "Nur Buchstaben erlaubt";
        }
        if (pattern.equals("^[0-9]+$")) {
            return "Nur Zahlen erlaubt";
        }
        if (pattern.equals("^[A-Za-z0-9]+$")) {
            return "Nur Buchstaben und Zahlen erlaubt";
        }
        
        return null;
    }
    
    /**
     * Registers a custom pattern description.
     * 
     * @param pattern the regex pattern
     * @param description the human-readable description
     */
    public void registerPattern(String pattern, String description) {
        knownPatterns.put(pattern, description);
    }
}