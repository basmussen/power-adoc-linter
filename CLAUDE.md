# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Test Commands

```bash
# Build the project
mvn clean compile

# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=YamlConfigurationLoaderTest

# Package the application
mvn clean package

# Check for dependency updates
mvn versions:display-dependency-updates
```

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

### Key Components

1. **LinterConfiguration**: Root configuration object containing document rules
2. **DocumentConfiguration**: Defines metadata requirements and section structure
3. **SectionConfiguration**: Hierarchical section definitions with allowed blocks
4. **ValidationRule**: Base class for all validation rules with severity levels (ERROR, WARN, INFO)

### Testing Strategy

- JUnit 5 for all tests
- Test resources in `src/test/resources/config/`
- Comprehensive test coverage including error cases
- Integration test loads the full specification

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

1. **New Block Types**: Extend `AbstractBlock` and follow existing patterns
2. **New Validation Rules**: Create specific rule classes extending base rules
3. **Configuration Extensions**: Update YAML loader and add corresponding tests

### Prototype Development Approach

- Start with small, focused implementations
- Validate patterns before extending to other classes
- Interview-based planning for major operations (German communication preferred)
- Incremental development with frequent validation

## Current Implementation Status

- ✅ YAML configuration parser
- ✅ Hierarchical configuration structure  
- ✅ Type-specific block classes
- ✅ Validation rules framework
- ⏳ AsciiDoc document parsing
- ⏳ Rule execution engine
- ⏳ CLI interface