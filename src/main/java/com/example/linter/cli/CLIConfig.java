package com.example.linter.cli;

import java.nio.file.Path;
import java.util.Objects;

import com.example.linter.config.Severity;

/**
 * Configuration object containing parsed CLI arguments.
 */
public class CLIConfig {
    
    private final Path input;
    private final Path configFile;
    private final String reportFormat;
    private final Path reportOutput;
    private final boolean recursive;
    private final String pattern;
    private final Severity failLevel;
    
    private CLIConfig(Builder builder) {
        this.input = Objects.requireNonNull(builder.input, "input must not be null");
        this.configFile = builder.configFile;
        this.reportFormat = Objects.requireNonNull(builder.reportFormat, "reportFormat must not be null");
        this.reportOutput = builder.reportOutput;
        this.recursive = builder.recursive;
        this.pattern = Objects.requireNonNull(builder.pattern, "pattern must not be null");
        this.failLevel = Objects.requireNonNull(builder.failLevel, "failLevel must not be null");
    }
    
    public Path getInput() {
        return input;
    }
    
    public Path getConfigFile() {
        return configFile;
    }
    
    public String getReportFormat() {
        return reportFormat;
    }
    
    public Path getReportOutput() {
        return reportOutput;
    }
    
    public boolean isRecursive() {
        return recursive;
    }
    
    public String getPattern() {
        return pattern;
    }
    
    public Severity getFailLevel() {
        return failLevel;
    }
    
    public boolean isOutputToFile() {
        return reportOutput != null;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Path input;
        private Path configFile;
        private String reportFormat = "console";
        private Path reportOutput;
        private boolean recursive = true;
        private String pattern = "*.adoc";
        private Severity failLevel = Severity.ERROR;
        
        public Builder input(Path input) {
            this.input = input;
            return this;
        }
        
        public Builder configFile(Path configFile) {
            this.configFile = configFile;
            return this;
        }
        
        public Builder reportFormat(String reportFormat) {
            this.reportFormat = reportFormat;
            return this;
        }
        
        public Builder reportOutput(Path reportOutput) {
            this.reportOutput = reportOutput;
            return this;
        }
        
        public Builder recursive(boolean recursive) {
            this.recursive = recursive;
            return this;
        }
        
        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }
        
        public Builder failLevel(Severity failLevel) {
            this.failLevel = failLevel;
            return this;
        }
        
        public CLIConfig build() {
            return new CLIConfig(this);
        }
    }
}