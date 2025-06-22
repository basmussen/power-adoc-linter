# Schema-basierte Korrekturen der Dokumentation

## Wichtige Korrekturen

### 1. Metadaten-Struktur
**ALT (falsch):**
```yaml
metadata:
  required: [...]
  optional: [...]
  patterns: {...}
  severity: error
```

**NEU (korrekt nach Schema):**
```yaml
metadata:
  attributes:
    - name: title
      required: true
      pattern: "..."
      severity: error
    - name: author
      required: false
      severity: warn
```

### 2. Output-Konfiguration
**ALT (falsch):**
```yaml
output:
  contextLines: 3
  colors: true
```

**NEU (korrekt nach Schema):**
```yaml
output:
  format: enhanced
  display:
    contextLines: 3
    useColors: true
    showLineNumbers: true
  suggestions:
    enabled: true
```

### 3. Severity-Werte
- Nur `error`, `warn`, `info` sind erlaubt
- NICHT `warning` verwenden
- Jedes Attribut benötigt eigene Severity

### 4. Entfernte Features
- `globalBlocks` existiert nicht im Schema
- `custom` und `optional` Arrays für Metadaten existieren nicht
- Patterns werden direkt im Attribut definiert, nicht separat

## Validierung
Alle Beispiele wurden gegen die Schemas in `src/main/resources/schemas/` validiert.