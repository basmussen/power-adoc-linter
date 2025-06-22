# Output Configuration Examples

This directory contains example output configuration files for the Power AsciiDoc Linter.

## Available Configurations

### enhanced-output.yaml
Full-featured output with maximum detail, visual highlighting, and suggestions. Best for interactive development.

```bash
java -jar power-adoc-linter.jar -i "**/*.adoc" --output-config examples/output-configs/enhanced-output.yaml
```

### simple-output.yaml
Clean, minimal output without colors or visual elements. Good for logs or when color support is limited.

```bash
java -jar power-adoc-linter.jar -i "**/*.adoc" --output-config examples/output-configs/simple-output.yaml
```

### ci-output.yaml
Compact output optimized for CI/CD pipelines. Machine-readable format with minimal formatting.

```bash
java -jar power-adoc-linter.jar -i "**/*.adoc" --output-config examples/output-configs/ci-output.yaml
```

### grouped-output.yaml
Groups similar errors together to reduce noise in large projects. Shows sample errors from each group.

```bash
java -jar power-adoc-linter.jar -i "**/*.adoc" --output-config examples/output-configs/grouped-output.yaml
```

## Configuration Structure

Output configurations control how validation results are displayed in the console. They do not affect which rules are applied (that's controlled by the linter configuration).

Key configuration options:
- **format**: Output format style (enhanced, simple, compact)
- **display**: Visual display settings (colors, context, highlighting)
- **errorGrouping**: Controls error grouping behavior
- **summary**: Summary section configuration

## Creating Custom Configurations

You can create your own output configuration by copying one of these examples and modifying it to suit your needs. The configuration is validated against the output configuration schema.