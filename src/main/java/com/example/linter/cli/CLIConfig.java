package com.example.linter.cli;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import com.example.linter.config.Severity;

/**
 * Configuration object containing parsed CLI arguments.
 */
public class CLIConfig {
    
    private final List<String> inputPatterns;
    private final Path baseDirectory;
    private final Path configFile;
    private final Path outputConfigFile;
    private final String reportFormat;
    private final Path reportOutput;
    private final Severity failLevel;
    
    private CLIConfig(Builder builder) {
        this.inputPatterns = Objects.requireNonNull(builder.inputPatterns, "inputPatterns must not be null");
        if (this.inputPatterns.isEmpty()) {
            throw new IllegalArgumentException("inputPatterns must not be empty");
        }
        this.baseDirectory = Objects.requireNonNull(builder.baseDirectory, "baseDirectory must not be null");
        this.configFile = builder.configFile;
        this.outputConfigFile = builder.outputConfigFile;
        this.reportFormat = Objects.requireNonNull(builder.reportFormat, "reportFormat must not be null");
        this.reportOutput = builder.reportOutput;
        this.failLevel = Objects.requireNonNull(builder.failLevel, "failLevel must not be null");
    }
    
    public List<String> getInputPatterns() {
        return inputPatterns;
    }
    
    public Path getBaseDirectory() {
        return baseDirectory;
    }
    
    public Path getConfigFile() {
        return configFile;
    }
    
    public Path getOutputConfigFile() {
        return outputConfigFile;
    }
    
    public String getReportFormat() {
        return reportFormat;
    }
    
    public Path getReportOutput() {
        return reportOutput;
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
        private List<String> inputPatterns;
        private Path baseDirectory = Paths.get(System.getProperty("user.dir"));
        private Path configFile;
        private Path outputConfigFile;
        private String reportFormat = "console";
        private Path reportOutput;
        private Severity failLevel = Severity.ERROR;
        
        public Builder inputPatterns(List<String> inputPatterns) {
            this.inputPatterns = inputPatterns;
            return this;
        }
        
        public Builder baseDirectory(Path baseDirectory) {
            this.baseDirectory = baseDirectory;
            return this;
        }
        
        public Builder configFile(Path configFile) {
            this.configFile = configFile;
            return this;
        }
        
        public Builder outputConfigFile(Path outputConfigFile) {
            this.outputConfigFile = outputConfigFile;
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
        
        public Builder failLevel(Severity failLevel) {
            this.failLevel = failLevel;
            return this;
        }
        
        public CLIConfig build() {
            return new CLIConfig(this);
        }
    }
}