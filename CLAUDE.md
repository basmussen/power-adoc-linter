# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run all tests with code coverage
mvn clean test

# Generate JaCoCo coverage report
mvn clean test jacoco:report
# View coverage report: target/site/jacoco/index.html

# View coverage summary in terminal
mvn clean test jacoco:report && grep -A20 "Total" target/site/jacoco/index.html

# Check coverage thresholds (will fail if below configured minimums)
mvn clean test jacoco:check

# Run a specific test class
mvn test -Dtest=ConfigurationLoaderTest
mvn test -Dtest=BlockValidatorTest
mvn test -Dtest=SectionValidatorTest

# Run tests from a specific package
mvn test -Dtest="com.example.linter.config.blocks.*"
mvn test -Dtest="com.example.linter.validator.*"
mvn test -Dtest="com.example.linter.validator.block.*"

# Run tests matching a pattern
mvn test -Dtest="*ValidatorTest"

# Package the application
mvn clean package

# Check for dependency updates
mvn versions:display-dependency-updates

# Run build without tests
mvn clean install -DskipTests

# Run with specific Java version if multiple installed
JAVA_HOME=/path/to/java17 mvn clean test
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
- Mockito 5.14.2
- JaCoCo 0.8.13

## Architecture Overview

This is a **prototype** AsciiDoc linter built with Java 17 and Maven. The linter validates AsciiDoc documents against configurable rules defined in YAML.

### Core Design Patterns

1. **Builder Pattern**: All configuration classes and validators use immutable builders
   - Example: `LinterConfiguration.builder().build()`
   - Every config class has a static `builder()` method
   - Builders use `Objects.requireNonNull()` for required fields
   - Validators use `fromConfiguration()` factory methods

2. **Inheritance Hierarchy**: 
   - Block types extend `AbstractBlock`
   - Concrete implementations: `ParagraphBlock`, `ListingBlock`, `TableBlock`, `ImageBlock`, `VerseBlock`
   - Each block type has specific validation rules as inner classes
   - Validation rules implement `AttributeRule` interface

3. **Configuration Loading**: YAML-based configuration through `ConfigurationLoader`
   - Supports file and stream-based loading
   - Hierarchical structure: LinterConfiguration → DocumentConfiguration → Sections/Metadata → Blocks/Rules
   - Uses SnakeYAML with custom constructors for complex types

4. **Validator Pattern**: Each block type has a corresponding validator
   - Interface: `BlockTypeValidator`
   - Factory: `BlockValidatorFactory` creates validators based on block type
   - Context: `BlockValidationContext` tracks validation state

### Package Structure

- `com.example.linter.config`: Core configuration classes and enums
  - `BlockType`, `Severity` enums
  - `LinterConfiguration`, `DocumentConfiguration`, `MetadataConfiguration`
- `com.example.linter.config.blocks`: Type-specific block implementations
  - Each extends `AbstractBlock`
  - Contains inner classes for validation rules
- `com.example.linter.config.loader`: YAML configuration loading
  - `ConfigurationLoader` with custom YAML constructors
  - `ConfigurationException` for loading errors
- `com.example.linter.config.rule`: Reusable rule configurations
  - `OrderConfig`, `LineConfig`, `OccurrenceConfig`, `AttributeConfig`, `SectionConfig`, `TitleConfig`
- `com.example.linter.validator`: Core validation framework
  - `ValidationResult`, `ValidationMessage`, `SourceLocation`
  - `MetadataValidator`, `SectionValidator`, `BlockValidator` orchestrators
- `com.example.linter.validator.block`: Block-level validators
  - One validator per block type
  - Supporting validators: `BlockOccurrenceValidator`, `BlockOrderValidator`
  - `BlockTypeDetector` for runtime type detection
- `com.example.linter.validator.rules`: Generic validation rules
  - `RequiredRule`, `PatternRule`, `LengthRule`, `OrderRule`
  - All implement `AttributeRule` interface

### Key Components

1. **Configuration Hierarchy**:
   ```
   LinterConfiguration
   └── DocumentConfiguration
       ├── MetadataConfiguration (required attributes)
       └── List<SectionConfiguration>
           └── List<AbstractBlock> (allowed blocks with rules)
   ```

2. **Validation Flow**:
   ```
   Document → MetadataValidator → validates attributes
           → SectionValidator → validates sections
           → BlockValidator → orchestrates block validation
               ├── BlockOccurrenceValidator
               ├── BlockOrderValidator
               └── BlockTypeValidator (per type)
   ```

3. **Block Validation Rules**:
   - `ParagraphBlock`: line count validation (min/max)
   - `ListingBlock`: language requirements, title patterns, callout restrictions
   - `TableBlock`: dimensions, headers, captions, format validation
   - `ImageBlock`: URL patterns, dimensions, alt text requirements
   - `VerseBlock`: author/attribution requirements, content length

4. **Severity Levels**: ERROR, WARN, INFO for all validation rules

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
- Mockito for mocking AsciidoctorJ objects
- Code coverage tracked with JaCoCo (target: 70% line, 65% branch)
- Test assertions use exact string matches, not contains()
- AsciidoctorJ section levels: == is Level 1, === is Level 2

### Code Coverage Requirements

Configured in pom.xml with JaCoCo:
- Bundle level: 70% line coverage, 65% branch coverage
- Package level: 60% line coverage
- Class level: 50% line coverage (excludes *Test and *Builder classes)

Current coverage gaps (prioritize these for testing):
- Config domain objects (especially equals/hashCode methods)
- VerseBlockValidator edge cases
- BlockValidatorFactory
- ParagraphBlock configuration class

## Development Guidelines

### Git Workflow

- Use gitflow: master, develop, feature/*, bugfix/* branches
- Feature branches: `feature/{issue-number}-{description}`
- Commit format: `#{issue-number} {single sentence description}`
- Example: `#4 Implement SectionValidator with hierarchical section structure validation`

### Code Conventions

- Use Java 17 features (switch expressions, text blocks, records where appropriate)
- Immutable objects with Builder pattern
- Null safety with `Objects.requireNonNull()` in constructors/builders
- Proper equals/hashCode implementations (use Objects.hash, handle nulls)
- No comments in production code unless absolutely necessary
- Exception handling: wrap in domain exceptions, provide context
- All validation messages must include actualValue and expectedValue

### Adding New Features

1. **New Block Types**: 
   - Extend `AbstractBlock` in `config.blocks` package
   - Create inner classes for type-specific rules
   - Implement proper builders with `Objects.requireNonNull` checks
   - Create corresponding validator implementing `BlockTypeValidator`
   - Register validator in `BlockValidatorFactory`
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
   - Add validator logic in corresponding BlockValidator

4. **Configuration Extensions**: 
   - Update `ConfigurationLoader` to parse new attributes
   - Add test cases in `ConfigurationLoaderTest`
   - Update `linter-config-specification.yaml` with examples
   - Create JSON schema in `src/main/resources/schemas/`

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
- ✅ MetadataValidator - validates document metadata attributes
- ✅ SectionValidator - validates section structure, hierarchy, and ordering
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
- ✅ ValidationResult and reporting framework
- ✅ JaCoCo code coverage analysis configured
- ✅ AsciidoctorJ document parsing integration
- ⏳ Rule execution engine for complete document validation
- ⏳ CLI interface

## Important Files

- **Configuration Specification**: `docs/linter-config-specification.yaml` - Full example configuration
- **Schema Definitions**: 
  - `src/main/resources/schemas/blocks/*.yaml` - JSON Schema 2020-12 for each block type
  - `src/main/resources/schemas/sections/*.yaml` - JSON Schema for section definitions
- **Test Configuration**: `src/test/resources/test-config.yaml` - Test configuration examples
- **Main Test Classes**:
  - `ConfigurationLoaderTest` - YAML parsing examples
  - `MetadataValidatorTest` - Metadata validation patterns
  - `SectionValidatorTest` - Section structure validation
  - `BlockValidatorTest` - Block validation orchestration
  - Individual `*BlockValidatorTest` classes - Specific validation logic

## Debug Notes

- Debug tests werden nicht mit git commit hinzugefügt. Diese werden nach dem Beheben des Fehlers wieder gelöscht

## Validation Considerations

- Validation error messages should include:
  - Actual values found during validation
  - Expected values or criteria for validation
  - File location (line/column) when available
- Use `ValidationMessage.builder()` with all relevant fields
- Group related validations to minimize passes over the document
- Test assertions should use exact string comparisons, not contains()

## Exception Handling

- All validators should handle exceptions gracefully
- Catch exceptions from AsciidoctorJ API calls (getContent(), getAttribute(), etc.)
- Convert to validation errors rather than failing completely
- Log exceptions for debugging but continue validation
- Wrap attribute access in try-catch blocks:
  ```java
  try {
      Object value = block.getAttribute("name");
      // process value
  } catch (Exception e) {
      // treat as missing/invalid attribute
  }
  ```

## Common Pitfalls

1. **AsciidoctorJ API Differences**:
   - `Block` vs `StructuralNode` - validators receive `StructuralNode`
   - Attribute names vary: `attribution` vs `author`, `citetitle` vs `attribution`
   - Content access methods: `getContent()` may return null or throw exceptions

2. **Pattern Matching**:
   - Always null-check Pattern objects before calling `matcher()`
   - Handle Pattern serialization specially in equals/hashCode

3. **Test Data**:
   - Mock AsciidoctorJ objects return `Object` for attributes, not `String`
   - Use `when(mock.getAttribute("name")).thenReturn("value")` not primitive types
   - BlockValidationContext.trackBlock() takes (AbstractBlock, StructuralNode) in that order
