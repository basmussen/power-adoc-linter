package com.example.linter.documentation;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.example.linter.config.DocumentConfiguration;
import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.MetadataConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.rule.AttributeConfig;
import com.example.linter.config.rule.SectionConfig;

/**
 * Generates AsciiDoc documentation from linter configuration rules.
 * 
 * <p>This generator creates human-readable documentation in AsciiDoc format,
 * suitable for content authors who need to understand the validation rules
 * for their documents.</p>
 */
public class AsciiDocRuleGenerator implements RuleDocumentationGenerator {
    
    private final Set<VisualizationStyle> visualizationStyles;
    private final PatternHumanizer patternHumanizer;
    private final HierarchyVisualizerFactory visualizerFactory;
    
    /**
     * Creates a new AsciiDoc rule generator with default visualization styles.
     */
    public AsciiDocRuleGenerator() {
        this(Set.of(VisualizationStyle.TREE));
    }
    
    /**
     * Creates a new AsciiDoc rule generator with specified visualization styles.
     * 
     * @param visualizationStyles the visualization styles to use
     */
    public AsciiDocRuleGenerator(Set<VisualizationStyle> visualizationStyles) {
        this.visualizationStyles = new HashSet<>(visualizationStyles);
        this.patternHumanizer = new PatternHumanizer();
        this.visualizerFactory = new HierarchyVisualizerFactory();
    }
    
    @Override
    public void generate(LinterConfiguration config, PrintWriter writer) {
        Objects.requireNonNull(config, "config must not be null");
        Objects.requireNonNull(writer, "writer must not be null");
        
        generateHeader(writer);
        generateIntroduction(writer);
        generateMetadataSection(config.document().metadata(), writer);
        generateStructureSection(config.document(), writer);
        generateBlockReferenceSection(config.document(), writer);
        generateValidationLevelsSection(writer);
        generateTipsSection(writer);
    }
    
    @Override
    public DocumentationFormat getFormat() {
        return DocumentationFormat.ASCIIDOC;
    }
    
    @Override
    public String getName() {
        return "AsciiDoc Rule Documentation Generator";
    }
    
    private void generateHeader(PrintWriter writer) {
        writer.println("= AsciiDoc Dokument-Richtlinien");
        writer.println(":toc: left");
        writer.println(":toclevels: 3");
        writer.println(":icons: font");
        writer.println(":source-highlighter: rouge");
        writer.println();
    }
    
    private void generateIntroduction(PrintWriter writer) {
        writer.println("== Einführung");
        writer.println();
        writer.println("Diese Richtlinien beschreiben die Anforderungen für AsciiDoc-Dokumente in diesem Projekt.");
        writer.println("Alle Dokumente werden automatisch gegen diese Regeln validiert.");
        writer.println();
    }
    
    private void generateMetadataSection(MetadataConfiguration metadata, PrintWriter writer) {
        if (metadata == null || metadata.attributes() == null || metadata.attributes().isEmpty()) {
            return;
        }
        
        writer.println("== Dokument-Metadaten");
        writer.println();
        writer.println("Jedes Dokument muss die folgenden Metadaten-Attribute definieren:");
        writer.println();
        
        // Separate required and optional attributes
        List<AttributeConfig> requiredAttrs = metadata.attributes().stream()
            .filter(AttributeConfig::required)
            .toList();
        
        List<AttributeConfig> optionalAttrs = metadata.attributes().stream()
            .filter(attr -> !attr.required())
            .toList();
        
        if (!requiredAttrs.isEmpty()) {
            writer.println("=== Pflichtattribute");
            writer.println();
            generateAttributeTable(requiredAttrs, writer);
        }
        
        if (!optionalAttrs.isEmpty()) {
            writer.println("=== Optionale Attribute");
            writer.println();
            generateAttributeTable(optionalAttrs, writer);
        }
    }
    
    private void generateAttributeTable(List<AttributeConfig> attributes, PrintWriter writer) {
        writer.println("[cols=\"1,2,1,3\", options=\"header\"]");
        writer.println("|===");
        writer.println("|Attribut |Beschreibung |Schweregrad |Anforderungen");
        writer.println();
        
        for (AttributeConfig attr : attributes) {
            writer.println("|" + attr.name());
            writer.println("|" + getAttributeDescription(attr));
            writer.println("|" + formatSeverity(attr.severity()));
            writer.println("a|");
            generateAttributeRequirements(attr, writer);
            writer.println();
        }
        
        writer.println("|===");
        writer.println();
    }
    
    private void generateAttributeRequirements(AttributeConfig attr, PrintWriter writer) {
        if (attr.required()) {
            writer.println("* Pflichtfeld");
        } else {
            writer.println("* Optional");
        }
        
        if (attr.minLength() != null) {
            writer.println("* Mindestlänge: " + attr.minLength() + " Zeichen");
        }
        
        if (attr.maxLength() != null) {
            writer.println("* Maximallänge: " + attr.maxLength() + " Zeichen");
        }
        
        if (attr.pattern() != null) {
            writer.println("* Format: " + patternHumanizer.humanize(attr.pattern()));
        }
        
        // TODO: Add support for allowed values when available in AttributeConfig
        
        writer.println("* Beispiel: `:" + attr.name() + ": " + generateAttributeExample(attr) + "`");
    }
    
    private void generateStructureSection(DocumentConfiguration document, PrintWriter writer) {
        writer.println("== Dokumentstruktur");
        writer.println();
        
        // Generate hierarchy visualization based on selected styles
        for (VisualizationStyle style : visualizationStyles) {
            HierarchyVisualizer visualizer = visualizerFactory.create(style);
            writer.println("=== " + style.getDescription());
            writer.println();
            visualizer.visualize(LinterConfiguration.builder().document(document).build(), writer);
            writer.println();
        }
        
        // Generate detailed section documentation
        if (document.sections() != null && !document.sections().isEmpty()) {
            writer.println("== Abschnitt-Details");
            writer.println();
            
            for (SectionConfig section : document.sections()) {
                generateSectionDetails(section, writer);
            }
        }
    }
    
    private void generateSectionDetails(SectionConfig section, PrintWriter writer) {
        writer.println("=== Abschnitt: " + section.name());
        writer.println();
        
        generateSectionNote(section, writer);
        
        if (section.allowedBlocks() != null && !section.allowedBlocks().isEmpty()) {
            writer.println(".Erlaubte Inhalte");
            writer.println("[cols=\"1,3\", options=\"header\"]");
            writer.println("|===");
            writer.println("|Block-Typ |Anforderungen");
            writer.println();
            
            for (Block block : section.allowedBlocks()) {
                writer.println("|" + formatBlockType(block));
                writer.println("a|");
                generateBlockSummary(block, writer);
                writer.println();
            }
            
            writer.println("|===");
            writer.println();
        }
        
        generateSectionExample(section, writer);
        
        // Generate subsections
        if (section.subsections() != null && !section.subsections().isEmpty()) {
            for (SectionConfig subsection : section.subsections()) {
                generateSectionDetails(subsection, writer);
            }
        }
    }
    
    private void generateSectionNote(SectionConfig section, PrintWriter writer) {
        String severity = section.min() > 0 ? "CAUTION" : "NOTE";
        
        writer.println("[" + severity + "]");
        writer.println("====");
        writer.println("**Position**: " + (section.order() != null ? 
            "Position " + section.order() : "Beliebig"));
        writer.println("**Level**: " + section.level());
        writer.println("**Pflicht**: " + (section.min() > 0 ? "Ja" : "Nein"));
        
        writer.print("**Anzahl**: ");
        if (section.max() > 0) {
            writer.println(section.min() + "-" + section.max());
        } else {
            writer.println("Mindestens " + section.min());
        }
        
        writer.println("====");
        writer.println();
    }
    
    private void generateBlockReferenceSection(DocumentConfiguration document, PrintWriter writer) {
        writer.println("== Block-Referenz");
        writer.println();
        writer.println("Detaillierte Beschreibung aller verfügbaren Block-Typen und ihrer Validierungsregeln.");
        writer.println();
        
        // TODO: Collect all unique block types from configuration and generate detailed docs
    }
    
    private void generateValidationLevelsSection(PrintWriter writer) {
        writer.println("== Validierungsstufen");
        writer.println();
        
        writer.println("[cols=\"1,1,3\", options=\"header\"]");
        writer.println("|===");
        writer.println("|Stufe |Symbol |Bedeutung");
        writer.println();
        writer.println("|ERROR");
        writer.println("|icon:times-circle[role=\"red\"]");
        writer.println("|Dokument wird abgelehnt, muss korrigiert werden");
        writer.println();
        writer.println("|WARN");
        writer.println("|icon:exclamation-triangle[role=\"yellow\"]");
        writer.println("|Sollte behoben werden, Dokument wird aber akzeptiert");
        writer.println();
        writer.println("|INFO");
        writer.println("|icon:info-circle[role=\"blue\"]");
        writer.println("|Hinweis zur Verbesserung, optional");
        writer.println("|===");
        writer.println();
    }
    
    private void generateTipsSection(PrintWriter writer) {
        writer.println("== Tipps für Autoren");
        writer.println();
        writer.println("TIP: Nutzen Sie den Linter während des Schreibens mit `--watch` Modus.");
        writer.println();
        writer.println("TIP: Die Fehlermeldungen enthalten immer die erwarteten Werte.");
        writer.println();
        writer.println("IMPORTANT: Bei Fragen zu den Regeln wenden Sie sich an das Documentation Team.");
        writer.println();
    }
    
    // Helper methods
    private String getAttributeDescription(AttributeConfig attr) {
        // TODO: Load from schema or generate based on name
        return switch (attr.name()) {
            case "title" -> "Dokumenttitel";
            case "author" -> "Autor des Dokuments";
            case "version" -> "Dokumentversion";
            case "email" -> "Kontakt E-Mail";
            default -> attr.name();
        };
    }
    
    private String formatSeverity(Severity severity) {
        return severity.name();
    }
    
    private String generateAttributeExample(AttributeConfig attr) {
        return switch (attr.name()) {
            case "title" -> "Benutzerhandbuch für Power AsciiDoc Linter";
            case "author" -> "Max Mustermann";
            case "version" -> "1.0.0";
            case "email" -> "autor@beispiel.de";
            default -> "Beispielwert";
        };
    }
    
    private String formatBlockType(Block block) {
        return block.getType().toValue();
    }
    
    private void generateBlockSummary(Block block, PrintWriter writer) {
        // TODO: Generate summary based on block type
        writer.println("* Anzahl: " + getOccurrenceText(block));
        if (block.getSeverity() != null) {
            writer.println("* Schweregrad: " + block.getSeverity());
        }
    }
    
    private String getOccurrenceText(Block block) {
        if (block.getOccurrence() == null) {
            return "Beliebig";
        }
        
        Integer min = block.getOccurrence().min();
        Integer max = block.getOccurrence().max();
        
        if (min != null && max != null) {
            return min + "-" + max;
        } else if (min != null) {
            return "Mindestens " + min;
        } else if (max != null) {
            return "Maximal " + max;
        }
        return "Beliebig";
    }
    
    private void generateSectionExample(SectionConfig section, PrintWriter writer) {
        writer.println(".Beispiel");
        writer.println("[source,asciidoc]");
        writer.println("----");
        
        // Generate example based on section level
        String prefix = "=".repeat(section.level() + 1);
        writer.println(prefix + " " + section.name());
        writer.println();
        writer.println("Beispielinhalt für diesen Abschnitt.");
        
        writer.println("----");
        writer.println();
    }
}