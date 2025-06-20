package com.example.linter.cli;

import org.apache.commons.cli.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CLIOptions")
class CLIOptionsTest {
    
    private CLIOptions cliOptions;
    private CommandLineParser parser;
    
    @BeforeEach
    void setUp() {
        cliOptions = new CLIOptions();
        parser = new DefaultParser();
    }
    
    @Test
    @DisplayName("should parse short form arguments")
    void shouldParseShortFormArguments() throws ParseException {
        // Given
        String[] args = {"-i", "test.adoc", "-c", "config.yaml", "-f", "json", "-o", "report.json"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("i"));
        assertEquals("test.adoc", cmd.getOptionValue("i"));
        assertEquals("config.yaml", cmd.getOptionValue("c"));
        assertEquals("json", cmd.getOptionValue("f"));
        assertEquals("report.json", cmd.getOptionValue("o"));
    }
    
    @Test
    @DisplayName("should parse long form arguments")
    void shouldParseLongFormArguments() throws ParseException {
        // Given
        String[] args = {"--input", "test.adoc", "--config", "config.yaml", 
                        "--report-format", "json", "--report-output", "report.json"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("input"));
        assertEquals("test.adoc", cmd.getOptionValue("input"));
        assertEquals("config.yaml", cmd.getOptionValue("config"));
        assertEquals("json", cmd.getOptionValue("report-format"));
        assertEquals("report.json", cmd.getOptionValue("report-output"));
    }
    
    @Test
    @DisplayName("should require input parameter")
    void shouldRequireInputParameter() {
        // Given
        String[] args = {"-c", "config.yaml"};
        
        // When/Then
        assertThrows(ParseException.class, () -> 
            parser.parse(cliOptions.getOptions(), args));
    }
    
    @Test
    @DisplayName("should parse boolean flags")
    void shouldParseBooleanFlags() throws ParseException {
        // Given
        String[] args = {"-i", "test.adoc", "-r", "-h", "-v"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("recursive"));
        assertTrue(cmd.hasOption("help"));
        assertTrue(cmd.hasOption("version"));
    }
    
    @Test
    @DisplayName("should parse no-recursive flag")
    void shouldParseNoRecursiveFlag() throws ParseException {
        // Given
        String[] args = {"-i", "test.adoc", "--no-recursive"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertTrue(cmd.hasOption("no-recursive"));
        assertFalse(cmd.hasOption("recursive"));
    }
    
    @Test
    @DisplayName("should parse pattern and fail-level")
    void shouldParsePatternAndFailLevel() throws ParseException {
        // Given
        String[] args = {"-i", "test.adoc", "-p", "**/*.asciidoc", "-l", "warn"};
        
        // When
        CommandLine cmd = parser.parse(cliOptions.getOptions(), args);
        
        // Then
        assertEquals("**/*.asciidoc", cmd.getOptionValue("pattern"));
        assertEquals("warn", cmd.getOptionValue("fail-level"));
    }
}