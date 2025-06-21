package com.example.linter.cli;

import java.nio.file.Paths;

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
        
        // Input (required)
        String inputPath = cmd.getOptionValue("input");
        builder.input(Paths.get(inputPath));
        
        // Config file
        if (cmd.hasOption("config")) {
            builder.configFile(Paths.get(cmd.getOptionValue("config")));
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
        
        // Recursive
        if (cmd.hasOption("no-recursive")) {
            builder.recursive(false);
        } else if (cmd.hasOption("recursive")) {
            builder.recursive(true);
        }
        
        // Pattern
        if (cmd.hasOption("pattern")) {
            builder.pattern(cmd.getOptionValue("pattern"));
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
            "  " + PROGRAM_NAME + " -i README.adoc\n" +
            "  " + PROGRAM_NAME + " -i docs/ -f json -o report.json\n" +
            "  " + PROGRAM_NAME + " --input docs/ --config strict.yaml --fail-level warn\n" +
            "\nExit codes:\n" +
            "  0 - Success, no violations or only below fail level\n" +
            "  1 - Violations at or above fail level found\n" +
            "  2 - Invalid arguments or runtime error\n";
        
        formatter.printHelp(PROGRAM_NAME + " -i <file/directory> [options]", 
            header, options, footer, false);
    }
    
    private void printVersion() {
        System.out.println(PROGRAM_NAME + " version " + VERSION);
    }
}