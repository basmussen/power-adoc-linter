# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=ConfigurationLoaderTest
mvn test -Dtest=LinterTest

# Run tests from a specific package
mvn test -Dtest="com.example.linter.config.blocks.*"
mvn test -Dtest="com.example.linter.cli.*"
mvn test -Dtest="com.example.linter.validator.block.*"

# Package the application (creates executable JAR)
mvn clean package

# Run the linter CLI
java -jar target/power-adoc-linter.jar --help
java -jar target/power-adoc-linter.jar -i "**/*.adoc"
java -jar target/power-adoc-linter.jar -i "docs/**/*.adoc,examples/**/*.asciidoc" -c config.yaml

# Generate documentation from configuration
java -jar target/power-adoc-linter.jar --generate-docs -c config.yaml -o rules-documentation.adoc

# Check for dependency updates
mvn versions:display-dependency-updates

# Run build without tests
mvn clean install -DskipTests

# Run with JaCoCo coverage
mvn clean test jacoco:report
# View coverage report: target/site/jacoco/index.html

# Check if coverage meets thresholds
mvn clean verify

# Run with specific log level (via system property)
java -Dlog4j.configurationFile=src/main/resources/log4j2.xml -jar target/power-adoc-linter.jar -i document.adoc

# Create dependency tree
mvn dependency:tree

# Clean and rebuild shade JAR
mvn clean package -Pshade
```

## CLI Usage

```bash
# Basic usage - validate all AsciiDoc files recursively
java -jar target/power-adoc-linter.jar -i "**/*.adoc"

# Multiple patterns (comma-separated)
java -jar target/power-adoc-linter.jar -i "docs/**/*.adoc,examples/**/*.asciidoc,README.adoc"

# With custom configuration
java -jar target/power-adoc-linter.jar -i "src/*/docs/**/*.adoc" -c my-rules.yaml

# JSON output to file
java -jar target/power-adoc-linter.jar -i "**/*.adoc" -f json -o report.json

# Compact JSON output (single line)
java -jar target/power-adoc-linter.jar -i "**/*.adoc" -f json-compact

# Set fail level (default: error)
java -jar target/power-adoc-linter.jar -i "**/*.adoc" -l warn

# Generate documentation from configuration
java -jar target/power-adoc-linter.jar --generate-docs -c config.yaml

# Ant pattern examples:
# - "**/*.adoc" - all .adoc files in any directory
# - "docs/**/*.adoc" - all .adoc files under docs/
# - "*/docs/*.adoc" - .adoc files in any direct subdirectory's docs folder
# - "doc?.adoc" - matches doc1.adoc, doc2.adoc, but not docs.adoc
```

## Project Information

**Power AsciiDoc Linter** is a tool for checking and validating AsciiDoc documents against configurable rules defined in YAML.

### Prerequisites

- Java 17 or higher (Note: This project specifically requires Java 17, overriding the general Java 11+ requirement)
- Maven 3.6 or higher

### Technologies

- Java 17
- Maven
- AsciidoctorJ 2.5.13
- Jackson 2.18.2 (YAML parsing, replaced SnakeYAML)
- Apache Commons CLI 1.9.0
- Gson 2.13.1
- JaCoCo 0.8.13
- networknt json-schema-validator 1.5.7
- Log4j2 2.24.3
- SLF4J Bridge 2.0.16 (for AsciidoctorJ logging)

## Architecture Overview

This is a **prototype** AsciiDoc linter built with Java 17 and Maven. The linter validates AsciiDoc documents against configurable rules defined in YAML.

### Core Design Patterns

1. **Builder Pattern**: All configuration classes and validation results use immutable builders
   - Example: `LinterConfiguration.builder().build()`
   - Every config class has a static `builder()` method
   - Required fields enforced via `Objects.requireNonNull()` in builders

2. **Inheritance Hierarchy**: Block types extend `AbstractBlock`
   - Concrete implementations: `ParagraphBlock`, `ListingBlock`, `TableBlock`, `ImageBlock`, `VerseBlock`, `VideoBlock`, `AudioBlock`, `PassBlock`, `LiteralBlock`, `QuoteBlock`, `ExampleBlock`, `SidebarBlock`, `AdmonitionBlock`
   - Each block type has specific validation rules as inner classes
   - All blocks annotated with Jackson annotations for YAML parsing

3. **Configuration Loading**: YAML-based configuration through `ConfigurationLoader`
   - Uses Jackson with custom `BlockListDeserializer` for special YAML structure
   - Supports file and stream-based loading
   - Hierarchical structure: LinterConfiguration → DocumentConfiguration → Sections/Metadata
   - Integrated schema validation with networknt json-schema-validator
   - Skip validation with `new ConfigurationLoader(true)` for testing
   - Schema files in `src/main/resources/schemas/` using JSON Schema 2020-12

4. **Validator Pattern**: Each component has dedicated validators
   - `MetadataValidator`, `SectionValidator`, `BlockValidator`
   - Block-specific validators implementing `BlockTypeValidator`
   - Special validators: `BlockOrderValidator`, `BlockOccurrenceValidator`
   - All validators work based on YAML schema structure
   - Validation messages include actual vs expected values

5. **Strategy Pattern**: Report formatters
   - `ReportFormatter` interface with `ConsoleFormatter`, `JsonFormatter`, and `JsonCompactFormatter`
   - Factory pattern for formatter selection
   - Context rendering for showing surrounding lines in errors

6. **Severity Hierarchy**: Nested configuration severity overrides block-level severity
   - Optional severity in nested rules (e.g., `ListingBlock.LanguageConfig`)
   - Falls back to block-level severity when not specified
   - Enables fine-grained control over validation error levels

### Package Structure

- `com.example.linter`: Core linter class
- `com.example.linter.config`: Core configuration classes and enums
- `com.example.linter.config.blocks`: Type-specific block implementations
- `com.example.linter.config.loader`: YAML configuration loading with Jackson
- `com.example.linter.config.rule`: Reusable rule configurations
- `com.example.linter.config.validation`: Schema validation for configuration files
- `com.example.linter.config.output`: Output configuration for report formatting
- `com.example.linter.validator`: Core validation framework
- `com.example.linter.validator.block`: Block-level validators
- `com.example.linter.validator.rules`: Generic validation rules
- `com.example.linter.report`: Report generation (console, JSON)
- `com.example.linter.report.writer`: Report writing and context rendering
- `com.example.linter.cli`: Command-line interface
- `com.example.linter.documentation`: Rule documentation generation
- `com.example.linter.util`: Utilities (PatternHumanizer for readable error messages)

### Key Components

1. **Linter**: Central validation orchestrator
   - Provides `validateFile()`, `validateFiles()`, `validateDirectory()`
   - Manages AsciidoctorJ instance lifecycle
   - Source location tracking enabled via AsciidoctorJ's sourcemap feature

2. **LinterConfiguration**: Root configuration object containing document rules
   - Loaded via Jackson with full YAML support
   - All fields immutable with builder pattern
   - Optional output configuration for formatting

3. **DocumentConfiguration**: Defines metadata requirements and section structure

4. **SectionConfiguration**: Hierarchical section definitions with allowed blocks

5. **Block Types**: 
   - `ParagraphBlock`: Basic text blocks with line count and sentence validation
   - `ListingBlock`: Code blocks with language, title, and callout support
   - `TableBlock`: Tables with column/row counts, headers, captions
   - `ImageBlock`: Images with URL pattern, dimensions, alt text validation
   - `VerseBlock`: Quote/verse blocks with author and attribution
   - `VideoBlock`: Video blocks with URL patterns and optional captions
   - `AudioBlock`: Audio blocks with source validation
   - `PassBlock`: Passthrough blocks for raw content
   - `LiteralBlock`: Literal text blocks with formatting preservation
   - `QuoteBlock`: Quote blocks with attribution
   - `ExampleBlock`: Example blocks with optional captions
   - `SidebarBlock`: Sidebar content blocks
   - `AdmonitionBlock`: Admonition blocks (NOTE, TIP, WARNING, etc.)
   - All extend `AbstractBlock` with Jackson annotations

6. **Severity Levels**: ERROR, WARN, INFO for all validation rules
   - Block-level severity can be overridden by nested rules
   - Severity hierarchy pattern implemented across all validators

7. **CLI Components**:
   - `LinterCLI`: Main entry point with Apache Commons CLI
   - `CLIRunner`: Orchestrates validation based on CLI arguments
   - `FileDiscoveryService`: Finds files matching patterns
   - `DocumentationGenerator`: Generates rule documentation from configuration
   - Exit codes: 0 (success), 1 (validation failed), 2 (error)

8. **Configuration Loader**: 
   - Uses Jackson ObjectMapper with YAMLFactory
   - Custom `BlockListDeserializer` handles special YAML structure where block types are keys
   - Schema validation integrated but can be skipped for testing
   - Supports both file path and InputStream loading
   - Output configuration loading for report formatting

9. **Logging**:
   - Log4j2 configuration in `src/main/resources/log4j2.xml`
   - Separate console appenders for stdout and stderr
   - Package-specific loggers for fine-grained control
   - Suppresses verbose dependency logging

10. **Report Generation**:
    - `ContextRenderer`: Shows error context with surrounding lines
    - `ReportWriter`: Writes reports to console or file
    - `PatternHumanizer`: Converts regex patterns to human-readable descriptions
    - Supports console (with colors), JSON, and compact JSON formats

### Testing Strategy

- JUnit 5 with nested test classes for organization
- Test naming pattern: "should..." with @DisplayName annotations
- Given-When-Then structure in test methods
- Comprehensive equals/hashCode testing for all domain objects
- Builder pattern validation (null checks, required fields)
- JaCoCo code coverage (target: 70% line, 65% branch)
- Mockito for mocking AsciidoctorJ components
- Test files use consistent patterns for easy discovery

## Development Guidelines

### Git Workflow

- Use gitflow: master, develop, feature/*, bugfix/* branches
- Feature branches: `feature/{issue-number}-{description}`
- Commit format: `#{issue-number} {single sentence description}`
- Example: `#17 Implement CLI interface with Apache Commons CLI`

### Code Conventions

- Use Java 17 features (switch expressions, text blocks, records where appropriate)
- Immutable objects with Builder pattern
- Null safety with `Objects.requireNonNull()`
- Proper equals/hashCode implementations
- No comments in generated code
- All block type validators must reference YAML schema basis in Javadoc
- Validation messages must include actual and expected values

### Adding New Features

1. **New Block Types**: 
   - Extend `AbstractBlock`
   - Create inner classes for type-specific rules
   - Implement proper builders with `Objects.requireNonNull` checks
   - Add Jackson annotations (@JsonProperty, @JsonDeserialize, @JsonPOJOBuilder)
   - Create corresponding validator implementing `BlockTypeValidator`
   - Register in `BlockValidatorFactory`
   - Add comprehensive test class with nested test structure
   - Update `BlockListDeserializer` to handle new type
   - Create JSON schema in `src/main/resources/schemas/blocks/`

2. **New Validation Rules**: 
   - Create inner classes within the block type
   - Use builder pattern with optional severity field
   - Implement equals/hashCode properly (handle Pattern objects specially)
   - Add Jackson annotations for YAML parsing
   - Consider severity hierarchy (nested rule severity overrides block severity)
   - Include actual vs expected values in validation messages

3. **Configuration Extensions**: 
   - Add Jackson annotations to new configuration classes
   - Update `BlockListDeserializer` if adding new block types
   - Add test cases in `ConfigurationLoaderTest`
   - Update `linter-config-specification.yaml` with examples
   - Add JSON schema definition if new structure
   - Consider output configuration for formatting options

### Prototype Development Approach

- Start with small, focused implementations
- Validate patterns before extending to other classes
- Interview-based planning for major operations (German communication preferred)
- Incremental development with frequent validation

## Current Implementation Status

- ✅ YAML configuration parser with Jackson 2.18.2 (replaced SnakeYAML)
- ✅ Hierarchical configuration structure (Document → Sections → Blocks)
- ✅ Type-specific block classes with inner rule classes
- ✅ Validation rules framework with severity levels and hierarchy
- ✅ JSON Schema definitions for all block types
- ✅ AsciiDoc document parsing with AsciidoctorJ
- ✅ Complete validation framework (metadata, sections, blocks)
- ✅ Report generation (Console and JSON formats)
- ✅ CLI interface with Apache Commons CLI
- ✅ Executable JAR with Maven Shade Plugin
- ✅ Schema validation for configuration files (#12)
- ✅ Jackson migration with custom deserializers (#23)
- ✅ Logging framework with Log4j2
- ✅ Sentence validation for paragraph blocks (#28)
- ✅ JSON compact format for pipeline integration (#30)
- ✅ Context rendering for error messages (#65)
- ✅ Documentation generation from configuration
- ✅ GitHub Actions CI/CD pipeline
- ✅ Multiple output formats (enhanced, simple, compact)
- ✅ Configurable output styles

## Important Files

- **Configuration Specification**: `docs/linter-config-specification.yaml` - Full example configuration
- **Schema Definitions**: `src/main/resources/schemas/` - JSON Schema 2020-12 definitions
- **Custom Deserializer**: `src/main/java/com/example/linter/config/loader/BlockListDeserializer.java` - Handles special YAML structure
- **Test Examples**: `src/test/java/com/example/linter/config/loader/ConfigurationLoaderTest.java` - Shows YAML configuration patterns
- **Main Entry Point**: `src/main/java/com/example/linter/cli/LinterCLI.java`
- **Log Configuration**: `src/main/resources/log4j2.xml` - Logging setup

## Git Workflow Notes

- `gh pr soll auch immer den issue closen`

## Debug Notes

- Debug tests werden nicht mit git commit hinzugefügt. Diese werden nach dem Beheben des Fehlers wieder gelöscht

## Validation Considerations

- Validation error messages should include:
  - Actual values found during validation
  - Expected values or criteria for validation
  - Consistent format across all validators
  - Human-readable pattern descriptions (via PatternHumanizer)

## Role Definition Memory

- Rolle: Du bist eine Java Architekt und Lead Developer mit excellenter Expertise und wendest Best Practices in den Bereichen an

## GitHub Pull Request Workflow

- Nach dem Erstellen eines GitHub Pull Requests (gh pr):
  - Prüfe die GitHub Actions des Pull Requests auf Fehler
  - Falls Fehler auftreten, behebe diese umgehend

## CI/CD Integration

The linter is tested in CI with multiple Java versions (17, 21) and includes:
- Build verification
- Code quality checks
- Dependency security scanning
- Test coverage enforcement

Example CI usage:
```yaml
- name: Run AsciiDoc Linter
  run: java -jar power-adoc-linter.jar -i "docs/**/*.adoc" -c .linter-config.yaml -f json-compact
```