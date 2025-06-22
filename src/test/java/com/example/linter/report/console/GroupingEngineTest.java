package com.example.linter.report.console;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.example.linter.config.Severity;
import com.example.linter.config.output.ErrorGroupingConfig;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;

/**
 * Tests for GroupingEngine.
 */
@DisplayName("GroupingEngine Tests")
class GroupingEngineTest {
    
    @Nested
    @DisplayName("Rule Grouping Tests")
    class RuleGroupingTests {
        
        private GroupingEngine engine;
        
        @BeforeEach
        void setUp() {
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(true)
                .groupByRule(true)
                .groupBySeverity(false)
                .groupByFile(false)
                .maxGroupSize(10)
                .build();
            engine = new GroupingEngine(config);
        }
        
        @Test
        @DisplayName("should group messages by rule ID")
        void shouldGroupMessagesByRuleId() {
            // Given
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule2", Severity.WARN, "file1.adoc"),
                createMessage("rule1", Severity.ERROR, "file2.adoc"),
                createMessage("rule2", Severity.WARN, "file2.adoc"),
                createMessage("rule1", Severity.ERROR, "file3.adoc")
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(2, groups.size());
            
            // Find rule1 group
            GroupingEngine.GroupKey rule1Key = groups.keySet().stream()
                .filter(key -> key.getDisplayName().contains("rule1"))
                .findFirst()
                .orElseThrow();
            
            assertEquals(3, groups.get(rule1Key).size());
            assertTrue(groups.get(rule1Key).stream()
                .allMatch(msg -> msg.getRuleId().equals("rule1")));
        }
        
        @Test
        @DisplayName("should respect max group size")
        void shouldRespectMaxGroupSize() {
            // Given
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(true)
                .groupByRule(true)
                .maxGroupSize(2)
                .build();
            engine = new GroupingEngine(config);
            
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule1", Severity.ERROR, "file2.adoc"),
                createMessage("rule1", Severity.ERROR, "file3.adoc") // Should overflow
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(2, groups.size()); // Original group + overflow
            
            // Check that no group exceeds max size
            groups.values().forEach(group -> 
                assertTrue(group.size() <= 2)
            );
        }
    }
    
    @Nested
    @DisplayName("Severity Grouping Tests")
    class SeverityGroupingTests {
        
        private GroupingEngine engine;
        
        @BeforeEach
        void setUp() {
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(true)
                .groupByRule(false)
                .groupBySeverity(true)
                .groupByFile(false)
                .build();
            engine = new GroupingEngine(config);
        }
        
        @Test
        @DisplayName("should group messages by severity")
        void shouldGroupMessagesBySeverity() {
            // Given
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule2", Severity.WARN, "file1.adoc"),
                createMessage("rule3", Severity.ERROR, "file2.adoc"),
                createMessage("rule4", Severity.INFO, "file2.adoc"),
                createMessage("rule5", Severity.WARN, "file3.adoc")
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(3, groups.size()); // ERROR, WARN, INFO
            
            // Verify each group contains only messages of same severity
            groups.forEach((key, msgs) -> {
                Severity firstSeverity = msgs.get(0).getSeverity();
                assertTrue(msgs.stream()
                    .allMatch(msg -> msg.getSeverity() == firstSeverity));
            });
        }
    }
    
    @Nested
    @DisplayName("File Grouping Tests")
    class FileGroupingTests {
        
        private GroupingEngine engine;
        
        @BeforeEach
        void setUp() {
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(true)
                .groupByRule(false)
                .groupBySeverity(false)
                .groupByFile(true)
                .build();
            engine = new GroupingEngine(config);
        }
        
        @Test
        @DisplayName("should group messages by file")
        void shouldGroupMessagesByFile() {
            // Given
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule2", Severity.WARN, "file1.adoc"),
                createMessage("rule3", Severity.ERROR, "file2.adoc"),
                createMessage("rule4", Severity.INFO, "file2.adoc"),
                createMessage("rule5", Severity.WARN, "file1.adoc")
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(2, groups.size()); // file1.adoc, file2.adoc
            
            // Verify each group contains only messages from same file
            groups.forEach((key, msgs) -> {
                String firstFile = msgs.get(0).getLocation().getFilename();
                assertTrue(msgs.stream()
                    .allMatch(msg -> msg.getLocation().getFilename().equals(firstFile)));
            });
        }
    }
    
    @Nested
    @DisplayName("Combined Grouping Tests")
    class CombinedGroupingTests {
        
        private GroupingEngine engine;
        
        @BeforeEach
        void setUp() {
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(true)
                .groupByRule(true)
                .groupBySeverity(true)
                .groupByFile(false)
                .build();
            engine = new GroupingEngine(config);
        }
        
        @Test
        @DisplayName("should group by multiple criteria")
        void shouldGroupByMultipleCriteria() {
            // Given
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule1", Severity.WARN, "file1.adoc"),  // Different severity
                createMessage("rule1", Severity.ERROR, "file2.adoc"), // Same rule and severity
                createMessage("rule2", Severity.ERROR, "file1.adoc")  // Different rule
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(3, groups.size()); // 3 unique combinations
            
            // Each group should have unique rule+severity combination
            groups.values().forEach(group -> {
                String firstRule = group.get(0).getRuleId();
                Severity firstSeverity = group.get(0).getSeverity();
                
                assertTrue(group.stream().allMatch(msg -> 
                    msg.getRuleId().equals(firstRule) && 
                    msg.getSeverity() == firstSeverity
                ));
            });
        }
    }
    
    @Nested
    @DisplayName("Disabled Grouping Tests")
    class DisabledGroupingTests {
        
        @Test
        @DisplayName("should not group when disabled")
        void shouldNotGroupWhenDisabled() {
            // Given
            ErrorGroupingConfig config = ErrorGroupingConfig.builder()
                .enabled(false)
                .build();
            GroupingEngine engine = new GroupingEngine(config);
            
            List<ValidationMessage> messages = Arrays.asList(
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule1", Severity.ERROR, "file1.adoc"),
                createMessage("rule1", Severity.ERROR, "file1.adoc")
            );
            
            // When
            Map<GroupingEngine.GroupKey, List<ValidationMessage>> groups = engine.groupMessages(messages);
            
            // Then
            assertEquals(3, groups.size()); // Each message in its own group
            groups.values().forEach(group -> 
                assertEquals(1, group.size())
            );
        }
    }
    
    private ValidationMessage createMessage(String ruleId, Severity severity, String filename) {
        return ValidationMessage.builder()
            .ruleId(ruleId)
            .severity(severity)
            .message("Test message")
            .location(SourceLocation.builder()
                .filename(filename)
                .line(10)
                .column(1)
                .build())
            .build();
    }
}