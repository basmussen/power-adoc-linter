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
        // Input file/directory (required)
        options.addOption(Option.builder("i")
            .longOpt("input")
            .hasArg()
            .argName("file/directory")
            .desc("AsciiDoc file or directory to validate")
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
        
        // Recursive
        options.addOption(Option.builder("r")
            .longOpt("recursive")
            .desc("Recursively scan directories (default: true)")
            .build());
        
        // No recursive
        options.addOption(Option.builder()
            .longOpt("no-recursive")
            .desc("Do not scan directories recursively")
            .build());
        
        // Pattern
        options.addOption(Option.builder("p")
            .longOpt("pattern")
            .hasArg()
            .argName("glob")
            .desc("File pattern glob (default: *.adoc)")
            .build());
        
        // Fail level
        options.addOption(Option.builder("l")
            .longOpt("fail-level")
            .hasArg()
            .argName("level")
            .desc("Exit code 1 on: error, warn, info (default: error)")
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