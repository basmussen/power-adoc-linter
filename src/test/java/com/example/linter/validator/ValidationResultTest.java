package com.example.linter.validator;

import com.example.linter.config.Severity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationResult")
class ValidationResultTest {
    
    private ValidationMessage errorMessage;
    private ValidationMessage warnMessage;
    private ValidationMessage infoMessage;
    private SourceLocation location1;
    private SourceLocation location2;
    
    @BeforeEach
    void setUp() {
        location1 = SourceLocation.builder()
            .filename("test1.adoc")
            .startLine(10)
            .startColumn(5)
            .build();
            
        location2 = SourceLocation.builder()
            .filename("test2.adoc")
            .startLine(20)
            .build();
            
        errorMessage = ValidationMessage.builder()
            .severity(Severity.ERROR)
            .ruleId("test.error")
            .location(location1)
            .message("Test error message")
            .actualValue("actual")
            .expectedValue("expected")
            .build();
            
        warnMessage = ValidationMessage.builder()
            .severity(Severity.WARN)
            .ruleId("test.warn")
            .location(location2)
            .message("Test warning message")
            .build();
            
        infoMessage = ValidationMessage.builder()
            .severity(Severity.INFO)
            .ruleId("test.info")
            .location(location1)
            .message("Test info message")
            .build();
    }
    
    @Nested
    @DisplayName("Builder")
    class BuilderTests {
        
        @Test
        @DisplayName("should create empty result")
        void shouldCreateEmptyResult() {
            // When
            ValidationResult result = ValidationResult.builder().build();
            
            // Then
            assertNotNull(result);
            assertTrue(result.getMessages().isEmpty());
            assertFalse(result.hasErrors());
            assertFalse(result.hasWarnings());
        }
        
        @Test
        @DisplayName("should add single message")
        void shouldAddSingleMessage() {
            // When
            ValidationResult result = ValidationResult.builder()
                .addMessage(errorMessage)
                .build();
            
            // Then
            assertEquals(1, result.getMessages().size());
            assertEquals(errorMessage, result.getMessages().get(0));
        }
        
        @Test
        @DisplayName("should add multiple messages")
        void shouldAddMultipleMessages() {
            // Given
            List<ValidationMessage> messages = Arrays.asList(errorMessage, warnMessage, infoMessage);
            
            // When
            ValidationResult result = ValidationResult.builder()
                .addMessages(messages)
                .build();
            
            // Then
            assertEquals(3, result.getMessages().size());
            assertTrue(result.getMessages().containsAll(messages));
        }
        
        @Test
        @DisplayName("should track timing automatically")
        void shouldTrackTimingAutomatically() throws InterruptedException {
            // Given
            ValidationResult.Builder builder = ValidationResult.builder();
            Thread.sleep(10); // Ensure some time passes
            
            // When
            ValidationResult result = builder.complete().build();
            
            // Then
            assertTrue(result.getValidationTimeMillis() >= 10);
        }
        
        @Test
        @DisplayName("should allow custom timing")
        void shouldAllowCustomTiming() {
            // Given
            long startTime = System.currentTimeMillis() - 1000;
            long endTime = System.currentTimeMillis();
            
            // When
            ValidationResult result = ValidationResult.builder()
                .startTime(startTime)
                .endTime(endTime)
                .build();
            
            // Then
            assertEquals(endTime - startTime, result.getValidationTimeMillis());
        }
        
        @Test
        @DisplayName("should throw on null message")
        void shouldThrowOnNullMessage() {
            // When/Then
            assertThrows(NullPointerException.class, () ->
                ValidationResult.builder().addMessage(null));
        }
        
        @Test
        @DisplayName("should throw on null messages collection")
        void shouldThrowOnNullMessagesCollection() {
            // When/Then
            assertThrows(NullPointerException.class, () ->
                ValidationResult.builder().addMessages(null));
        }
    }
    
    @Nested
    @DisplayName("Message Retrieval")
    class MessageRetrievalTests {
        
        private ValidationResult result;
        
        @BeforeEach
        void setUp() {
            result = ValidationResult.builder()
                .addMessage(errorMessage)
                .addMessage(warnMessage)
                .addMessage(infoMessage)
                .build();
        }
        
        @Test
        @DisplayName("should return all messages")
        void shouldReturnAllMessages() {
            // When
            List<ValidationMessage> messages = result.getMessages();
            
            // Then
            assertEquals(3, messages.size());
            assertTrue(messages.contains(errorMessage));
            assertTrue(messages.contains(warnMessage));
            assertTrue(messages.contains(infoMessage));
        }
        
        @Test
        @DisplayName("should return immutable message list")
        void shouldReturnImmutableMessageList() {
            // When/Then
            assertThrows(UnsupportedOperationException.class, () ->
                result.getMessages().add(errorMessage));
        }
        
        @Test
        @DisplayName("should filter messages by severity")
        void shouldFilterMessagesBySeverity() {
            // When
            List<ValidationMessage> errors = result.getMessagesBySeverity(Severity.ERROR);
            List<ValidationMessage> warnings = result.getMessagesBySeverity(Severity.WARN);
            List<ValidationMessage> infos = result.getMessagesBySeverity(Severity.INFO);
            
            // Then
            assertEquals(1, errors.size());
            assertEquals(errorMessage, errors.get(0));
            
            assertEquals(1, warnings.size());
            assertEquals(warnMessage, warnings.get(0));
            
            assertEquals(1, infos.size());
            assertEquals(infoMessage, infos.get(0));
        }
    }
    
    @Nested
    @DisplayName("Message Grouping")
    class MessageGroupingTests {
        
        private ValidationResult result;
        private ValidationMessage anotherErrorInFile1;
        private ValidationMessage errorInFile2;
        
        @BeforeEach
        void setUp() {
            anotherErrorInFile1 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("another.error")
                .location(SourceLocation.builder()
                    .filename("test1.adoc")
                    .startLine(15)
                    .build())
                .message("Another error")
                .build();
                
            errorInFile2 = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("file2.error")
                .location(location2)
                .message("Error in file 2")
                .build();
                
            result = ValidationResult.builder()
                .addMessage(errorMessage)
                .addMessage(anotherErrorInFile1)
                .addMessage(errorInFile2)
                .addMessage(infoMessage)
                .build();
        }
        
        @Test
        @DisplayName("should group messages by file")
        void shouldGroupMessagesByFile() {
            // When
            Map<String, List<ValidationMessage>> messagesByFile = result.getMessagesByFile();
            
            // Then
            assertEquals(2, messagesByFile.size());
            assertTrue(messagesByFile.containsKey("test1.adoc"));
            assertTrue(messagesByFile.containsKey("test2.adoc"));
            
            List<ValidationMessage> file1Messages = messagesByFile.get("test1.adoc");
            assertEquals(3, file1Messages.size());
            assertTrue(file1Messages.contains(errorMessage));
            assertTrue(file1Messages.contains(anotherErrorInFile1));
            assertTrue(file1Messages.contains(infoMessage));
            
            List<ValidationMessage> file2Messages = messagesByFile.get("test2.adoc");
            assertEquals(1, file2Messages.size());
            assertEquals(errorInFile2, file2Messages.get(0));
        }
        
        @Test
        @DisplayName("should return sorted map by filename")
        void shouldReturnSortedMapByFilename() {
            // When
            Map<String, List<ValidationMessage>> messagesByFile = result.getMessagesByFile();
            
            // Then
            List<String> filenames = messagesByFile.keySet().stream().toList();
            assertEquals("test1.adoc", filenames.get(0));
            assertEquals("test2.adoc", filenames.get(1));
        }
        
        @Test
        @DisplayName("should group messages by line")
        void shouldGroupMessagesByLine() {
            // When
            Map<Integer, List<ValidationMessage>> messagesByLine = result.getMessagesByLine("test1.adoc");
            
            // Then
            assertEquals(2, messagesByLine.size());
            assertTrue(messagesByLine.containsKey(10));
            assertTrue(messagesByLine.containsKey(15));
            
            assertEquals(2, messagesByLine.get(10).size()); // error and info
            assertEquals(1, messagesByLine.get(15).size()); // anotherError
        }
        
        @Test
        @DisplayName("should return empty map for unknown file")
        void shouldReturnEmptyMapForUnknownFile() {
            // When
            Map<Integer, List<ValidationMessage>> messagesByLine = result.getMessagesByLine("unknown.adoc");
            
            // Then
            assertTrue(messagesByLine.isEmpty());
        }
    }
    
    @Nested
    @DisplayName("Status Checks")
    class StatusChecksTests {
        
        @Test
        @DisplayName("should detect errors")
        void shouldDetectErrors() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .addMessage(errorMessage)
                .addMessage(warnMessage)
                .build();
            
            // Then
            assertTrue(result.hasErrors());
            assertTrue(result.hasWarnings());
        }
        
        @Test
        @DisplayName("should detect warnings without errors")
        void shouldDetectWarningsWithoutErrors() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .addMessage(warnMessage)
                .addMessage(infoMessage)
                .build();
            
            // Then
            assertFalse(result.hasErrors());
            assertTrue(result.hasWarnings());
        }
        
        @Test
        @DisplayName("should handle empty result")
        void shouldHandleEmptyResult() {
            // Given
            ValidationResult result = ValidationResult.builder().build();
            
            // Then
            assertFalse(result.hasErrors());
            assertFalse(result.hasWarnings());
        }
    }
    
    @Nested
    @DisplayName("Counting")
    class CountingTests {
        
        private ValidationResult result;
        
        @BeforeEach
        void setUp() {
            ValidationMessage anotherError = ValidationMessage.builder()
                .severity(Severity.ERROR)
                .ruleId("another.error")
                .location(location1)
                .message("Another error")
                .build();
                
            ValidationMessage anotherWarn = ValidationMessage.builder()
                .severity(Severity.WARN)
                .ruleId("another.warn")
                .location(location2)
                .message("Another warning")
                .build();
                
            result = ValidationResult.builder()
                .addMessage(errorMessage)
                .addMessage(anotherError)
                .addMessage(warnMessage)
                .addMessage(anotherWarn)
                .addMessage(infoMessage)
                .build();
        }
        
        @Test
        @DisplayName("should count errors correctly")
        void shouldCountErrorsCorrectly() {
            assertEquals(2, result.getErrorCount());
        }
        
        @Test
        @DisplayName("should count warnings correctly")
        void shouldCountWarningsCorrectly() {
            assertEquals(2, result.getWarningCount());
        }
        
        @Test
        @DisplayName("should count info messages correctly")
        void shouldCountInfoMessagesCorrectly() {
            assertEquals(1, result.getInfoCount());
        }
        
        @Test
        @DisplayName("should handle empty result")
        void shouldHandleEmptyResult() {
            // Given
            ValidationResult emptyResult = ValidationResult.builder().build();
            
            // Then
            assertEquals(0, emptyResult.getErrorCount());
            assertEquals(0, emptyResult.getWarningCount());
            assertEquals(0, emptyResult.getInfoCount());
        }
    }
    
    @Nested
    @DisplayName("Report Printing")
    class ReportPrintingTests {
        
        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        private final PrintStream originalOut = System.out;
        
        @BeforeEach
        void setUp() {
            System.setOut(new PrintStream(outputStream));
        }
        
        @Test
        @DisplayName("should print empty report")
        void shouldPrintEmptyReport() {
            // Given
            ValidationResult result = ValidationResult.builder().build();
            
            // When
            result.printReport();
            String output = outputStream.toString();
            
            // Then
            assertTrue(output.contains("Validation Report"));
            assertTrue(output.contains("No validation issues found"));
            assertTrue(output.contains("0 errors, 0 warnings, 0 info messages"));
        }
        
        @Test
        @DisplayName("should print report with messages")
        void shouldPrintReportWithMessages() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .addMessage(errorMessage)
                .addMessage(warnMessage)
                .addMessage(infoMessage)
                .build();
            
            // When
            result.printReport();
            String output = outputStream.toString();
            
            // Then
            assertTrue(output.contains("Validation Report"));
            assertTrue(output.contains("test1.adoc"));
            assertTrue(output.contains("test2.adoc"));
            assertTrue(output.contains("Test error message"));
            assertTrue(output.contains("Test warning message"));
            assertTrue(output.contains("Test info message"));
            assertTrue(output.contains("1 errors, 1 warnings, 1 info messages"));
        }
        
        @Test
        @DisplayName("should include timing in report")
        void shouldIncludeTimingInReport() {
            // Given
            ValidationResult result = ValidationResult.builder()
                .startTime(System.currentTimeMillis() - 500)
                .complete()
                .build();
            
            // When
            result.printReport();
            String output = outputStream.toString();
            
            // Then
            assertTrue(output.contains("Validation completed in"));
            assertTrue(output.contains("ms"));
        }
        
        @org.junit.jupiter.api.AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }
    }
}