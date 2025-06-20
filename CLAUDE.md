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
mvn test -Dtest=TableBlockTest

# Run tests from a specific package
mvn test -Dtest="com.example.linter.config.blocks.*"

# Package the application
mvn clean package

# Check for dependency updates
mvn versions:display-dependency-updates

# Run build without tests
mvn clean install -DskipTests
```

## Project Information

**Power AsciiDoc Linter** is a tool for checking and validating AsciiDoc documents.

### Prerequisites

- Java 17 or higher (Note: This project specifically requires Java 17, overriding the general Java 11+ requirement)
- Maven 3.6 or higher

### Technologies

- Java 17
- Maven
- AsciidoctorJ 2.5.13

## Architecture Overview

This is a **prototype** AsciiDoc linter built with Java 17 and Maven. The linter validates AsciiDoc documents against configurable rules defined in YAML.

### Core Design Patterns

1. **Builder Pattern**: All configuration classes use immutable builders
   - Example: `LinterConfiguration.builder().build()`
   - Every config class has a static `builder()` method

2. **Inheritance Hierarchy**: Block types extend `AbstractBlock`
   - Concrete implementations: `ParagraphBlock`, `ListingBlock`, `TableBlock`, `ImageBlock`, `VerseBlock`
   - Each block type has specific validation rules

3. **Configuration Loading**: YAML-based configuration through `YamlConfigurationLoader`
   - Supports file and stream-based loading
   - Hierarchical structure: LinterConfiguration → DocumentConfiguration → Sections/Metadata

### Package Structure

- `com.example.linter.config`: Core configuration classes and enums
- `com.example.linter.config.blocks`: Type-specific block implementations
- `com.example.linter.config.loader`: YAML configuration loading
- `com.example.linter.config.rule`: Rule configurations (OrderConfig, LineConfig, etc.)
- `com.example.linter.validator`: Validation framework and validators
- `com.example.linter.validator.block`: Block-level validators and support classes

### Key Components

1. **LinterConfiguration**: Root configuration object containing document rules
2. **DocumentConfiguration**: Defines metadata requirements and section structure
3. **SectionConfiguration**: Hierarchical section definitions with allowed blocks
4. **Block Types**: 
   - `ParagraphBlock`: Basic text blocks with line count validation
   - `ListingBlock`: Code blocks with language, title, and callout support
   - `TableBlock`: Tables with column/row counts, headers, captions
   - `ImageBlock`: Images with URL pattern, dimensions, alt text validation
   - `VerseBlock`: Quote/verse blocks with author and attribution
5. **Severity Levels**: ERROR, WARN, INFO for all validation rules

### Testing Strategy

- JUnit 5 with nested test classes for organization
- Test naming pattern: "should..." with @DisplayName annotations
- Given-When-Then structure in test methods
- Comprehensive equals/hashCode testing for all domain objects
- Builder pattern validation (null checks, required fields)
- Use test case patterns like Given-When-Then
- Use "should" method naming pattern for test methods

## Development Guidelines

### Git Workflow

- Use gitflow: master, develop, feature/*, bugfix/* branches
- Feature branches: `feature/{issue-number}-{description}`
- Commit format: `#{issue-number} {single sentence description}`
- Example: `#2 Implement YAML parser for linter configuration`

### Code Conventions

- Use Java 17 features (switch expressions, text blocks)
- Immutable objects with Builder pattern
- Null safety with `Objects.requireNonNull()`
- Proper equals/hashCode implementations
- No comments in generated code

### Adding New Features

1. **New Block Types**: 
   - Extend `AbstractBlock`
   - Create inner classes for type-specific rules
   - Implement proper builders with `Objects.requireNonNull` checks
   - Add comprehensive test class with nested test structure

2. **New Validation Rules**: 
   - Create inner classes within the block type
   - Use builder pattern with required severity field
   - Implement equals/hashCode properly (handle Pattern objects specially)

3. **Configuration Extensions**: 
   - Update `ConfigurationLoader` to parse new attributes
   - Add test cases in `ConfigurationLoaderTest`
   - Update `linter-config-specification.yaml` with examples

### Prototype Development Approach

- Start with small, focused implementations
- Validate patterns before extending to other classes
- Interview-based planning for major operations (German communication preferred)
- Incremental development with frequent validation

## Current Implementation Status

- ✅ YAML configuration parser with SnakeYAML 2.3
- ✅ Hierarchical configuration structure (Document → Sections → Blocks)
- ✅ Type-specific block classes with inner rule classes
- ✅ Validation rules framework with severity levels
- ✅ JSON Schema definitions for block types (in `src/main/resources/schemas/blocks/`)
- ✅ SectionValidator - validates section structure, title patterns, occurrences, subsections
- ✅ MetadataValidator - validates document metadata attributes
- ✅ Block-level validators with concrete implementations:
  - ✅ BlockTypeValidator interface and implementations for each block type
  - ✅ ParagraphBlockValidator - validates paragraph line counts
  - ✅ TableBlockValidator - validates table dimensions, headers, captions
  - ✅ ImageBlockValidator - validates image URLs, dimensions, alt text
  - ✅ ListingBlockValidator - validates code blocks with language, callouts
  - ✅ VerseBlockValidator - validates verse/quote blocks with author, attribution
  - ✅ BlockOccurrenceValidator - validates min/max block occurrences
  - ✅ BlockOrderValidator - validates block order constraints
  - ✅ BlockValidator orchestrator - coordinates all block validation
- ⏳ AsciiDoc document parsing with AsciidoctorJ integration
- ⏳ Rule execution engine
- ⏳ CLI interface

## Important Files

- **Configuration Specification**: `docs/linter-config-specification.yaml` - Full example configuration
- **Schema Definitions**: `src/main/resources/schemas/blocks/*.yaml` - JSON Schema 2020-12 for each block type
- **Test Examples**: `src/test/java/com/example/linter/config/loader/ConfigurationLoaderTest.java` - Shows YAML configuration patterns

## Debug Notes

- Debug tests werden nicht mit git commit hinzugefügt. Diese werden nach dem Beheben des Fehlers wieder gelöscht

## Validation Considerations

- Validation error messages should include:
  - Actual values found during validation
  - Expected values or criteria for validation