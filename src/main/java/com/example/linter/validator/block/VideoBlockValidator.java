package com.example.linter.validator.block;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.VideoBlock;
import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;
import org.asciidoctor.ast.StructuralNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validator for video blocks in AsciiDoc documents.
 * 
 * Validates video blocks based on the YAML schema configuration including:
 * - URL validation (required and pattern matching)
 * - Dimension constraints (width and height)
 * - Poster image validation
 * - Controls requirement
 * - Caption validation
 */
public class VideoBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.VIDEO;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode node, 
                                           Block blockConfig,
                                           BlockValidationContext context) {
        if (!(blockConfig instanceof VideoBlock)) {
            throw new IllegalArgumentException("Expected VideoBlock configuration");
        }
        
        VideoBlock videoConfig = (VideoBlock) blockConfig;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Validate URL
        if (videoConfig.getUrl() != null) {
            validateUrl(node, videoConfig, messages, context);
        }
        
        // Validate dimensions
        if (videoConfig.getWidth() != null) {
            validateDimension(node, videoConfig, "width", videoConfig.getWidth(), messages, context);
        }
        
        if (videoConfig.getHeight() != null) {
            validateDimension(node, videoConfig, "height", videoConfig.getHeight(), messages, context);
        }
        
        // Validate poster
        if (videoConfig.getPoster() != null) {
            validatePoster(node, videoConfig, messages, context);
        }
        
        // Validate options (controls)
        if (videoConfig.getOptions() != null && videoConfig.getOptions().getControls() != null) {
            validateControls(node, videoConfig, messages, context);
        }
        
        // Validate caption
        if (videoConfig.getCaption() != null) {
            validateCaption(node, videoConfig, messages, context);
        }
        
        return messages;
    }
    
    private void validateUrl(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.UrlConfig urlConfig = videoConfig.getUrl();
        String url = (String) node.getAttribute("target");
        
        // Determine severity
        Severity severity = urlConfig.getSeverity() != null ? 
            urlConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(urlConfig.getRequired()) && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.url.required")
                    .message("Video URL is required but not provided")
                    .location(context.createLocation(node))
                    .build());
            return;
        }
        
        // Check pattern
        if (url != null && urlConfig.getPattern() != null) {
            Pattern pattern = urlConfig.getPattern();
            if (!pattern.matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.url.pattern")
                        .message(String.format("Video URL '%s' does not match required pattern '%s'", 
                                url, pattern.pattern()))
                        .location(context.createLocation(node))
                        .build());
            }
        }
    }
    
    private void validateDimension(StructuralNode node, VideoBlock videoConfig, String dimensionType, 
                                  VideoBlock.DimensionConfig dimensionConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        String dimensionStr = (String) node.getAttribute(dimensionType);
        
        // Determine severity
        Severity severity = dimensionConfig.getSeverity() != null ? 
            dimensionConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(dimensionConfig.getRequired()) && 
            (dimensionStr == null || dimensionStr.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video." + dimensionType + ".required")
                    .message(String.format("Video %s is required but not provided", dimensionType))
                    .location(context.createLocation(node))
                    .build());
            return;
        }
        
        // Validate dimension value
        if (dimensionStr != null) {
            try {
                int value = Integer.parseInt(dimensionStr);
                
                if (dimensionConfig.getMinValue() != null && value < dimensionConfig.getMinValue()) {
                    messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId("video." + dimensionType + ".min")
                            .message(String.format("Video %s %d is below minimum value %d", 
                                    dimensionType, value, dimensionConfig.getMinValue()))
                            .location(context.createLocation(node))
                            .build());
                }
                
                if (dimensionConfig.getMaxValue() != null && value > dimensionConfig.getMaxValue()) {
                    messages.add(ValidationMessage.builder()
                            .severity(severity)
                            .ruleId("video." + dimensionType + ".max")
                            .message(String.format("Video %s %d exceeds maximum value %d", 
                                    dimensionType, value, dimensionConfig.getMaxValue()))
                            .location(context.createLocation(node))
                            .build());
                }
            } catch (NumberFormatException e) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video." + dimensionType + ".invalid")
                        .message(String.format("Video %s '%s' is not a valid number", 
                                dimensionType, dimensionStr))
                        .location(context.createLocation(node))
                        .build());
            }
        }
    }
    
    private void validatePoster(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.PosterConfig posterConfig = videoConfig.getPoster();
        String poster = (String) node.getAttribute("poster");
        
        // Determine severity
        Severity severity = posterConfig.getSeverity() != null ? 
            posterConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(posterConfig.getRequired()) && (poster == null || poster.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.poster.required")
                    .message("Video poster image is required but not provided")
                    .location(context.createLocation(node))
                    .build());
            return;
        }
        
        // Check pattern
        if (poster != null && posterConfig.getPattern() != null) {
            Pattern pattern = posterConfig.getPattern();
            if (!pattern.matcher(poster).matches()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.poster.pattern")
                        .message(String.format("Video poster '%s' does not match required pattern '%s'", 
                                poster, pattern.pattern()))
                        .location(context.createLocation(node))
                        .build());
            }
        }
    }
    
    private void validateControls(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.ControlsConfig controlsConfig = videoConfig.getOptions().getControls();
        String controlsAttr = (String) node.getAttribute("options");
        
        // Determine severity
        Severity severity = controlsConfig.getSeverity() != null ? 
            controlsConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if controls are required
        if (Boolean.TRUE.equals(controlsConfig.getRequired())) {
            boolean hasControls = controlsAttr != null && controlsAttr.contains("controls");
            
            if (!hasControls) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.controls.required")
                        .message("Video controls are required but not enabled")
                        .location(context.createLocation(node))
                        .build());
            }
        }
    }
    
    private void validateCaption(StructuralNode node, VideoBlock videoConfig, List<ValidationMessage> messages, BlockValidationContext context) {
        VideoBlock.CaptionConfig captionConfig = videoConfig.getCaption();
        String caption = (String) node.getAttribute("caption");
        
        // If no caption attribute, check for title
        if (caption == null) {
            caption = node.getTitle();
        }
        
        // Determine severity
        Severity severity = captionConfig.getSeverity() != null ? 
            captionConfig.getSeverity() : videoConfig.getSeverity();
        
        // Check if required
        if (Boolean.TRUE.equals(captionConfig.getRequired()) && (caption == null || caption.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .ruleId("video.caption.required")
                    .message("Video caption is required but not provided")
                    .location(context.createLocation(node))
                    .build());
            return;
        }
        
        // Check length constraints
        if (caption != null) {
            int length = caption.length();
            
            if (captionConfig.getMinLength() != null && length < captionConfig.getMinLength()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.caption.minLength")
                        .message(String.format("Video caption length %d is below minimum %d", 
                                length, captionConfig.getMinLength()))
                        .location(context.createLocation(node))
                        .build());
            }
            
            if (captionConfig.getMaxLength() != null && length > captionConfig.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                        .severity(severity)
                        .ruleId("video.caption.maxLength")
                        .message(String.format("Video caption length %d exceeds maximum %d", 
                                length, captionConfig.getMaxLength()))
                        .location(context.createLocation(node))
                        .build());
            }
        }
    }
}