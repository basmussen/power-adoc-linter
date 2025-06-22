package com.example.linter.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Defines command line options for the AsciiDoc linter CLI.
 */
public class CLIOptions {
    
    private final Options options;
    
    public CLIOptions() {
        this.options = new Options();
        defineOptions();
    }
    
    private void defineOptions() {
        // Input patterns (required)
        options.addOption(Option.builder("i")
            .longOpt("input")
            .hasArg()
            .argName("patterns")
            .desc("Comma-separated Ant file patterns (e.g., '**/*.adoc,docs/**/*.asciidoc')")
            .required()
            .build());
        
        // Configuration file
        options.addOption(Option.builder("c")
            .longOpt("config")
            .hasArg()
            .argName("file")
            .desc("YAML configuration file (default: .linter-config.yaml)")
            .build());
        
        // Report format
        options.addOption(Option.builder("f")
            .longOpt("report-format")
            .hasArg()
            .argName("format")
            .desc("Report format: console, json, json-compact (default: console)")
            .build());
        
        // Report output
        options.addOption(Option.builder("o")
            .longOpt("report-output")
            .hasArg()
            .argName("file/directory")
            .desc("Report output file or directory (default: stdout)")
            .build());
        
        // Fail level
        options.addOption(Option.builder("l")
            .longOpt("fail-level")
            .hasArg()
            .argName("level")
            .desc("Exit code 1 on: error, warn, info (default: error)")
            .build());
        
        // Output configuration
        options.addOption(Option.builder()
            .longOpt("output-config")
            .hasArg()
            .argName("file")
            .desc("YAML output configuration file for enhanced console formatting")
            .build());
        
        // Help
        options.addOption(Option.builder("h")
            .longOpt("help")
            .desc("Show help message")
            .build());
        
        // Version
        options.addOption(Option.builder("v")
            .longOpt("version")
            .desc("Show version")
            .build());
    }
    
    public Options getOptions() {
        return options;
    }
}