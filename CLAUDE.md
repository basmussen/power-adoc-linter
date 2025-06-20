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
mvn test -Dtest=SectionValidatorTest

# Run tests from a specific package
mvn test -Dtest="com.example.linter.config.blocks.*"
mvn test -Dtest="com.example.linter.validator.*"

# Package the application
mvn clean package

# Check for dependency updates
mvn versions:display-dependency-updates

# Run build without tests
mvn clean install -DskipTests
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
- SnakeYAML 2.3
- JUnit 5.13.1

## Architecture Overview

This is a **prototype** AsciiDoc linter built with Java 17 and Maven. The linter validates AsciiDoc documents against configurable rules defined in YAML.

### Core Design Patterns

1. **Builder Pattern**: All configuration classes and validators use immutable builders
   - Example: `LinterConfiguration.builder().build()`
   - Every config class has a static `builder()` method
   - Validators use `fromConfiguration()` factory methods

2. **Inheritance Hierarchy**: 
   - Block types extend `AbstractBlock`
   - Concrete implementations: `ParagraphBlock`, `ListingBlock`, `TableBlock`, `ImageBlock`, `VerseBlock`
   - Validation rules implement `AttributeRule` interface

3. **Configuration Loading**: YAML-based configuration through `YamlConfigurationLoader`
   - Hierarchical structure: LinterConfiguration → DocumentConfiguration → Sections/Metadata → Blocks/Rules

### Package Structure

- `com.example.linter.config`: Core configuration classes and enums
- `com.example.linter.config.blocks`: Type-specific block implementations
- `com.example.linter.config.rule`: Rule configurations (SectionConfig, TitleConfig, AttributeConfig)
- `com.example.linter.config.loader`: YAML configuration loading
- `com.example.linter.validator`: Document validators (MetadataValidator, SectionValidator)
- `com.example.linter.validator.rules`: Validation rule implementations

### Key Components

1. **Configuration Layer**:
   - `LinterConfiguration`: Root configuration object
   - `DocumentConfiguration`: Document-level rules
   - `SectionConfig`: Section definitions with hierarchy support
   - `MetadataConfiguration`: Metadata and attribute rules

2. **Validation Layer**:
   - `MetadataValidator`: Validates document metadata and attributes
   - `SectionValidator`: Validates section structure, hierarchy, and ordering
   - `ValidationResult`: Encapsulates all validation messages with timing
   - `ValidationMessage`: Individual validation issue with location and details

3. **Block Types**: 
   - Each block type has specific inner rule classes
   - All rules include severity levels (ERROR, WARN, INFO)
   - Rules support actualValue/expectedValue for clear error reporting

### Validation Architecture

- **Validators** compose multiple **Rules** for flexible validation
- **Rules** implement the `AttributeRule` interface:
  - `RequiredRule`: Validates attribute presence
  - `PatternRule`: Regex-based validation
  - `LengthRule`: Min/max length constraints
  - `OrderRule`: Attribute order validation
- All validation errors include both actualValue and expectedValue
- Precise source location tracking with line numbers

### Testing Strategy

- JUnit 5 with nested test classes for organization
- Test naming pattern: "should..." with @DisplayName annotations
- Comprehensive equals/hashCode testing for all domain objects
- Builder pattern validation (null checks, required fields)
- Test assertions use exact string matches, not contains()
- AsciidoctorJ section levels: == is Level 1, === is Level 2

## Development Guidelines

### Git Workflow

- Use gitflow: master, develop, feature/*, bugfix/* branches
- Feature branches: `feature/{issue-number}-{description}`
- Commit format: `#{issue-number} {single sentence description}`
- Example: `#4 Implement SectionValidator with hierarchical section structure validation`

### Code Conventions

- Use Java 17 features (switch expressions, text blocks)
- Immutable objects with Builder pattern
- Null safety with `Objects.requireNonNull()`
- Proper equals/hashCode implementations
- No comments in generated code
- All validation messages must include actualValue and expectedValue

### Adding New Features

1. **New Block Types**: 
   - Extend `AbstractBlock`
   - Create inner classes for type-specific rules
   - Implement proper builders with `Objects.requireNonNull` checks
   - Add comprehensive test class with nested test structure

2. **New Validators**:
   - Follow MetadataValidator/SectionValidator patterns
   - Use Builder pattern with `fromConfiguration()` factory
   - Compose validation rules for flexibility
   - Return `ValidationResult` with detailed messages

3. **New Validation Rules**: 
   - Implement `AttributeRule` interface
   - Use builder pattern with required severity field
   - Include actualValue/expectedValue in messages
   - Handle Pattern objects specially in equals/hashCode

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
- ✅ MetadataValidator for document metadata validation
- ✅ SectionValidator for section structure validation
- ✅ AsciidoctorJ document parsing integration
- ⏳ Rule execution engine for block content validation
- ⏳ CLI interface

## Important Files

- **Configuration Specification**: `docs/linter-config-specification.yaml` - Full example configuration
- **Schema Definitions**: `src/main/resources/schemas/` - JSON Schema 2020-12 for blocks and sections
- **Test Examples**: `src/test/java/com/example/linter/config/loader/ConfigurationLoaderTest.java` - YAML configuration patterns
- **Validator Tests**: `src/test/java/com/example/linter/validator/*Test.java` - Validation behavior examples

## Debug Notes

- Debug tests werden nicht mit git commit hinzugefügt. Diese werden nach dem Beheben des Fehlers wieder gelöscht

## Validation Considerations

- Validation error messages should include:
  - Actual values found during validation
  - Expected values or criteria for validation
- Test assertions should use exact string comparisons, not contains()