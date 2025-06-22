package com.example.linter.documentation.visualizers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.example.linter.config.DocumentConfiguration;
import com.example.linter.config.LinterConfiguration;
import com.example.linter.config.MetadataConfiguration;
import com.example.linter.config.Severity;
import com.example.linter.config.rule.AttributeConfig;
import com.example.linter.config.rule.SectionConfig;
import com.example.linter.config.blocks.Block;
import com.example.linter.documentation.HierarchyVisualizer;
import com.example.linter.documentation.VisualizationStyle;

/**
 * Visualizes rule hierarchy as a table with indentation.
 */
public class TableVisualizer implements HierarchyVisualizer {
    
    private static final String INDENT = "{nbsp}{nbsp}";
    
    private static class TableRow {
        final String rule;
        final int level;
        final Severity severity;
        final String type;
        final String requirements;
        
        TableRow(String rule, int level, Severity severity, String type, String requirements) {
            this.rule = rule;
            this.level = level;
            this.severity = severity;
            this.type = type;
            this.requirements = requirements;
        }
    }
    
    @Override
    public void visualize(LinterConfiguration config, PrintWriter writer) {
        List<TableRow> rows = collectTableRows(config.document());
        
        writer.println("[%header,cols=\"<4,^1,^1,2,3\"]");
        writer.println("|===");
        writer.println("|Regel |Level |Severity |Typ |Anforderungen");
        writer.println();
        
        for (TableRow row : rows) {
            writer.println("|" + getIndentedRule(row.rule, row.level));
            writer.println("|" + row.level);
            writer.println("|" + (row.severity != null ? row.severity : "—"));
            writer.println("|" + row.type);
            writer.println("|" + row.requirements);
            writer.println();
        }
        
        writer.println("|===");
        writer.println();
        writer.println("_* = Überschreibt übergeordnete Severity_");
    }
    
    @Override
    public VisualizationStyle getStyle() {
        return VisualizationStyle.TABLE;
    }
    
    private List<TableRow> collectTableRows(DocumentConfiguration doc) {
        List<TableRow> rows = new ArrayList<>();
        
        // Add document root
        rows.add(new TableRow("**DOKUMENT**", 0, null, "Root", "Basis-Konfiguration"));
        
        // Add metadata
        if (doc.metadata() != null) {
            collectMetadataRows(doc.metadata(), 1, rows);
        }
        
        // Add sections
        if (doc.sections() != null && !doc.sections().isEmpty()) {
            rows.add(new TableRow("└ sections", 1, Severity.ERROR, "Container", "Mind. 1 Section"));
            
            for (SectionConfig section : doc.sections()) {
                collectSectionRows(section, 2, rows);
            }
        }
        
        return rows;
    }
    
    private void collectMetadataRows(MetadataConfiguration metadata, int level, 
                                    List<TableRow> rows) {
        rows.add(new TableRow("└ metadata", level, Severity.ERROR, "Container", "Pflicht"));
        
        if (metadata.attributes() != null) {
            for (int i = 0; i < metadata.attributes().size(); i++) {
                AttributeConfig attr = metadata.attributes().get(i);
                boolean isLast = i == metadata.attributes().size() - 1;
                String prefix = isLast ? "└ " : "├ ";
                
                String requirements = buildAttributeRequirements(attr);
                rows.add(new TableRow(prefix + attr.name(), level + 1, attr.severity(), 
                                     "Attribut", requirements));
            }
        }
    }
    
    private void collectSectionRows(SectionConfig section, int level, 
                                   List<TableRow> rows) {
        String requirements = "Level " + section.level();
        if (section.order() != null) {
            requirements += ", Position " + section.order();
        }
        
        Severity severity = section.min() > 0 ? 
                          Severity.ERROR : Severity.INFO;
        
        rows.add(new TableRow("└ " + section.name(), level, severity, "Section", requirements));
        
        if (section.allowedBlocks() != null && !section.allowedBlocks().isEmpty()) {
            rows.add(new TableRow("├ allowedBlocks", level + 1, null, "Container", 
                                 "Block-Definitionen"));
            
            for (int i = 0; i < section.allowedBlocks().size(); i++) {
                Block block = section.allowedBlocks().get(i);
                boolean isLast = i == section.allowedBlocks().size() - 1;
                
                collectBlockRows(block, level + 2, isLast, rows);
            }
        }
        
        if (section.subsections() != null && !section.subsections().isEmpty()) {
            rows.add(new TableRow("└ subsections", level + 1, null, "Container", 
                                 "Unterabschnitte"));
            
            for (SectionConfig subsection : section.subsections()) {
                collectSectionRows(subsection, level + 2, rows);
            }
        }
    }
    
    private void collectBlockRows(Block block, int level, boolean isLast, 
                                 List<TableRow> rows) {
        String prefix = isLast ? "└ " : "├ ";
        String requirements = getBlockDescription(block);
        
        rows.add(new TableRow(prefix + block.getType().toValue(), level, 
                             block.getSeverity(), "Block", requirements));
        
        // Add block-specific rules
        // TODO: Add specific rules based on block type with severity override indicators
    }
    
    private String getIndentedRule(String rule, int level) {
        StringBuilder indented = new StringBuilder();
        for (int i = 0; i < level; i++) {
            indented.append(INDENT);
        }
        indented.append(rule);
        return indented.toString();
    }
    
    private String buildAttributeRequirements(AttributeConfig attr) {
        List<String> reqs = new ArrayList<>();
        
        if (attr.required()) {
            reqs.add("Pflicht");
        } else {
            reqs.add("Optional");
        }
        
        if (attr.minLength() != null || attr.maxLength() != null) {
            if (attr.minLength() != null && attr.maxLength() != null) {
                reqs.add(attr.minLength() + "-" + attr.maxLength() + " Zeichen");
            } else if (attr.minLength() != null) {
                reqs.add("Min. " + attr.minLength() + " Zeichen");
            } else {
                reqs.add("Max. " + attr.maxLength() + " Zeichen");
            }
        }
        
        if (attr.pattern() != null) {
            reqs.add("Muster definiert");
        }
        
        return String.join(", ", reqs);
    }
    
    private String getBlockDescription(Block block) {
        List<String> desc = new ArrayList<>();
        
        if (block.getName() != null) {
            desc.add(block.getName());
        }
        
        if (block.getOccurrence() != null) {
            Integer min = block.getOccurrence().min();
            Integer max = block.getOccurrence().max();
            if (min != null && max != null) {
                desc.add(min + "-" + max + " mal");
            } else if (min != null) {
                desc.add("Min. " + min + " mal");
            } else if (max != null) {
                desc.add("Max. " + max + " mal");
            }
        }
        
        return desc.isEmpty() ? "Keine speziellen Anforderungen" : String.join(", ", desc);
    }
}