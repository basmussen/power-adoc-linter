package com.example.linter.cli;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.example.linter.config.Severity;

/**
 * Main CLI entry point for the AsciiDoc linter.
 */
public class LinterCLI {
    
    private static final Logger logger = LogManager.getLogger(LinterCLI.class);
    private static final String VERSION = "1.0.0";
    private static final String PROGRAM_NAME = "power-adoc-linter";
    
    public static void main(String[] args) {
        LinterCLI cli = new LinterCLI();
        int exitCode = cli.run(args);
        System.exit(exitCode);
    }
    
    public int run(String[] args) {
        CLIOptions cliOptions = new CLIOptions();
        Options options = cliOptions.getOptions();
        
        try {
            CommandLineParser parser = new DefaultParser();
            CommandLine cmd = parser.parse(options, args);
            
            // Handle help
            if (cmd.hasOption("help")) {
                printHelp(options);
                return 0;
            }
            
            // Handle version
            if (cmd.hasOption("version")) {
                printVersion();
                return 0;
            }
            
            // Handle documentation generation
            if (cmd.hasOption("generate-docs")) {
                // Input is not required for doc generation
                if (!cmd.hasOption("config")) {
                    System.err.println("Error: --config is required when using --generate-docs");
                    return 2;
                }
                
                DocumentationGenerator docGenerator = new DocumentationGenerator();
                return docGenerator.run(cmd);
            }
            
            // For normal validation, input is required
            if (!cmd.hasOption("input")) {
                System.err.println("Error: --input is required for validation");
                printHelp(options);
                return 2;
            }
            
            // Parse configuration
            CLIConfig config = parseConfiguration(cmd);
            
            // Run linter
            CLIRunner runner = new CLIRunner();
            return runner.run(config);
            
        } catch (ParseException e) {
            logger.error("Error: {}", e.getMessage());
            System.err.println("Error: " + e.getMessage());
            System.err.println();
            printHelp(options);
            return 2;
        } catch (IllegalArgumentException e) {
            logger.error("Error: {}", e.getMessage());
            System.err.println("Error: " + e.getMessage());
            return 2;
        }
    }
    
    private CLIConfig parseConfiguration(CommandLine cmd) {
        CLIConfig.Builder builder = CLIConfig.builder();
        
        // Input patterns (required)
        String inputValue = cmd.getOptionValue("input");
        List<String> patterns = Arrays.stream(inputValue.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toList());
        
        if (patterns.isEmpty()) {
            throw new IllegalArgumentException("No input patterns provided");
        }
        
        builder.inputPatterns(patterns);
        
        // Config file
        if (cmd.hasOption("config")) {
            builder.configFile(Paths.get(cmd.getOptionValue("config")));
        }
        
        // Output config file
        if (cmd.hasOption("output-config")) {
            builder.outputConfigFile(Paths.get(cmd.getOptionValue("output-config")));
        }
        
        // Report format
        if (cmd.hasOption("report-format")) {
            String format = cmd.getOptionValue("report-format");
            if (!format.equals("console") && !format.equals("json") && !format.equals("json-compact")) {
                throw new IllegalArgumentException("Invalid report format: " + format + 
                    ". Valid values are: console, json, json-compact");
            }
            builder.reportFormat(format);
        }
        
        // Report output
        if (cmd.hasOption("report-output")) {
            builder.reportOutput(Paths.get(cmd.getOptionValue("report-output")));
        }
        
        // Fail level
        if (cmd.hasOption("fail-level")) {
            String level = cmd.getOptionValue("fail-level").toUpperCase();
            try {
                builder.failLevel(Severity.valueOf(level));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid fail level: " + level + 
                    ". Valid values are: error, warn, info");
            }
        }
        
        return builder.build();
    }
    
    private void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.setWidth(100);
        
        String header = "\nValidates AsciiDoc files against configurable rules.\n\n";
        String footer = "\nExamples:\n" +
            "  " + PROGRAM_NAME + " -i \"**/*.adoc\"\n" +
            "  " + PROGRAM_NAME + " -i \"docs/**/*.adoc,examples/**/*.asciidoc\" -f json -o report.json\n" +
            "  " + PROGRAM_NAME + " --input \"src/*/docs/**/*.adoc,README.adoc\" --config strict.yaml --fail-level warn\n" +
            "  " + PROGRAM_NAME + " -i \"**/*.adoc\" --output-config enhanced-output.yaml\n" +
            "\nAnt Pattern Syntax:\n" +
            "  **  - matches any number of directories\n" +
            "  *   - matches any number of characters (except /)\n" +
            "  ?   - matches exactly one character\n" +
            "\nExit codes:\n" +
            "  0 - Success, no violations or only below fail level\n" +
            "  1 - Violations at or above fail level found\n" +
            "  2 - Invalid arguments or runtime error\n";
        
        formatter.printHelp(PROGRAM_NAME + " -i <patterns> [options]", 
            header, options, footer, false);
    }
    
    private void printVersion() {
        System.out.println(PROGRAM_NAME + " version " + VERSION);
    }
}