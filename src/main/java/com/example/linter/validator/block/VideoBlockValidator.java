package com.example.linter.validator.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.asciidoctor.ast.StructuralNode;

import com.example.linter.config.BlockType;
import com.example.linter.config.Severity;
import com.example.linter.config.blocks.Block;
import com.example.linter.config.blocks.VideoBlock;
import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.SourceLocation;

/**
 * Validator for video blocks in AsciiDoc documents.
 * 
 * <p>Validates video blocks based on the YAML schema structure where
 * block types are keys and their configurations are nested objects.</p>
 */
public class VideoBlockValidator implements BlockTypeValidator {
    
    @Override
    public BlockType getSupportedType() {
        return BlockType.VIDEO;
    }
    
    @Override
    public List<ValidationMessage> validate(StructuralNode block, Block blockConfig, 
                                          BlockValidationContext context) {
        if (!(blockConfig instanceof VideoBlock)) {
            throw new IllegalArgumentException("Config must be VideoBlock, got: " + 
                (blockConfig == null ? "null" : blockConfig.getClass().getName()));
        }
        
        VideoBlock config = (VideoBlock) blockConfig;
        List<ValidationMessage> messages = new ArrayList<>();
        
        // Extract video attributes
        String videoUrl = getVideoUrl(block);
        
        // Validate URL
        if (config.getUrl() != null) {
            validateUrl(videoUrl, config.getUrl(), config, context, block, messages);
        }
        
        // Validate dimensions
        if (config.getWidth() != null) {
            validateDimension("width", block.getAttribute("width"), config.getWidth(), 
                            config, context, block, messages);
        }
        
        if (config.getHeight() != null) {
            validateDimension("height", block.getAttribute("height"), config.getHeight(), 
                            config, context, block, messages);
        }
        
        // Validate poster
        if (config.getPoster() != null) {
            validatePoster(block.getAttribute("poster"), config.getPoster(), 
                         config, context, block, messages);
        }
        
        // Validate options
        if (config.getOptions() != null) {
            validateOptions(block, config.getOptions(), config, context, block, messages);
        }
        
        // Validate caption
        if (config.getCaption() != null) {
            validateCaption(block.getTitle(), config.getCaption(), 
                          config, context, block, messages);
        }
        
        return messages;
    }
    
    private String getVideoUrl(StructuralNode block) {
        // Try to get URL from different possible sources
        Object url = block.getAttribute("target");
        if (url == null) {
            url = block.getAttribute("source");
        }
        if (url == null && block.getContent() instanceof String) {
            url = block.getContent();
        }
        return url != null ? url.toString() : null;
    }
    
    private void validateUrl(String url, VideoBlock.UrlConfig config,
                           VideoBlock blockConfig,
                           BlockValidationContext context,
                           StructuralNode block,
                           List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (url == null || url.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .message("Video URL is required")
                .location(context.createLocation(block))
                .ruleId(getRuleId(blockConfig, "url-required"))
                .actualValue("missing")
                .expectedValue("valid URL")
                .build());
            return;
        }
        
        if (url != null && !url.trim().isEmpty() && config.getPattern() != null) {
            Pattern pattern = config.getPattern();
            if (!pattern.matcher(url).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video URL does not match pattern")
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, "url-pattern"))
                    .actualValue(url)
                    .expectedValue("pattern: " + pattern.pattern())
                    .build());
            }
        }
    }
    
    private void validateDimension(String dimensionName, Object value, 
                                 VideoBlock.DimensionConfig config,
                                 VideoBlock blockConfig,
                                 BlockValidationContext context,
                                 StructuralNode block,
                                 List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && value == null) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .message("Video " + dimensionName + " is required")
                .location(context.createLocation(block))
                .ruleId(getRuleId(blockConfig, dimensionName + "-required"))
                .actualValue("missing")
                .expectedValue("numeric value")
                .build());
            return;
        }
        
        if (value != null) {
            Integer dimension = parseInteger(value);
            if (dimension == null) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video " + dimensionName + " must be a valid number")
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, dimensionName + "-invalid"))
                    .actualValue(value.toString())
                    .expectedValue("numeric value")
                    .build());
                return;
            }
            
            if (config.getMinValue() != null && dimension < config.getMinValue()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video " + dimensionName + " " + dimension + " is less than minimum " + config.getMinValue())
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, dimensionName + "-min"))
                    .actualValue(dimension.toString())
                    .expectedValue("minimum: " + config.getMinValue())
                    .build());
            }
            
            if (config.getMaxValue() != null && dimension > config.getMaxValue()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video " + dimensionName + " exceeds maximum")
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, dimensionName + "-max"))
                    .actualValue(dimension.toString())
                    .expectedValue("maximum: " + config.getMaxValue())
                    .build());
            }
        }
    }
    
    private void validatePoster(Object posterValue, VideoBlock.PosterConfig config,
                              VideoBlock blockConfig,
                              BlockValidationContext context,
                              StructuralNode block,
                              List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        String poster = posterValue != null ? posterValue.toString() : null;
        
        if (config.isRequired() && (poster == null || poster.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .message("Video poster image is required")
                .location(context.createLocation(block))
                .ruleId(getRuleId(blockConfig, "poster-required"))
                .actualValue("missing")
                .expectedValue("image URL")
                .build());
            return;
        }
        
        if (poster != null && !poster.trim().isEmpty() && config.getPattern() != null) {
            Pattern pattern = config.getPattern();
            if (!pattern.matcher(poster).matches()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video poster image does not match required pattern")
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, "poster-pattern"))
                    .actualValue(poster)
                    .expectedValue("pattern: " + pattern.pattern())
                    .build());
            }
        }
    }
    
    private void validateOptions(StructuralNode block, 
                               VideoBlock.OptionsConfig config,
                               VideoBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode blockNode,
                               List<ValidationMessage> messages) {
        
        // Validate autoplay
        if (config.getAutoplay() != null) {
            validateAutoplay(block, config.getAutoplay(), blockConfig, context, blockNode, messages);
        }
        
        // Validate controls
        if (config.getControls() != null) {
            validateControls(block, config.getControls(), blockConfig, context, blockNode, messages);
        }
    }
    
    private void validateAutoplay(StructuralNode block,
                                VideoBlock.AutoplayConfig config,
                                VideoBlock blockConfig,
                                BlockValidationContext context,
                                StructuralNode blockNode,
                                List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if autoplay is present in options
        Object optionsObj = block.getAttribute("options");
        String options = optionsObj != null ? optionsObj.toString() : null;
        boolean hasAutoplay = options != null && options.contains("autoplay");
        
        if (config.getAllowed() != null && !config.getAllowed() && hasAutoplay) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .message("Video autoplay is not allowed")
                .location(context.createLocation(blockNode))
                .ruleId(getRuleId(blockConfig, "autoplay-not-allowed"))
                .actualValue("autoplay enabled")
                .expectedValue("autoplay disabled")
                .build());
        }
    }
    
    private void validateControls(StructuralNode block,
                                VideoBlock.ControlsConfig config,
                                VideoBlock blockConfig,
                                BlockValidationContext context,
                                StructuralNode blockNode,
                                List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        // Check if controls are explicitly disabled
        Object optionsObj = block.getAttribute("options");
        String options = optionsObj != null ? optionsObj.toString() : null;
        boolean noControls = options != null && options.contains("nocontrols");
        
        // If required, check if controls are NOT present or explicitly disabled
        if (config.isRequired()) {
            boolean hasControls = options != null && options.contains("controls");
            if (noControls) {
                // Controls explicitly disabled
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video controls are required")
                    .location(context.createLocation(blockNode))
                    .ruleId(getRuleId(blockConfig, "controls-required"))
                    .actualValue("controls disabled")
                    .expectedValue("controls enabled")
                    .build());
            } else if (!hasControls) {
                // Controls not present in options
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video controls are required")
                    .location(context.createLocation(blockNode))
                    .ruleId(getRuleId(blockConfig, "controls-required"))
                    .actualValue("controls not specified")
                    .expectedValue("controls enabled")
                    .build());
            }
        }
    }
    
    private void validateCaption(String caption, VideoBlock.CaptionConfig config,
                               VideoBlock blockConfig,
                               BlockValidationContext context,
                               StructuralNode block,
                               List<ValidationMessage> messages) {
        
        Severity severity = config.getSeverity() != null ? config.getSeverity() : blockConfig.getSeverity();
        
        if (config.isRequired() && (caption == null || caption.trim().isEmpty())) {
            messages.add(ValidationMessage.builder()
                .severity(severity)
                .message("Video caption is required")
                .location(context.createLocation(block))
                .ruleId(getRuleId(blockConfig, "caption-required"))
                .actualValue("missing")
                .expectedValue("caption text")
                .build());
            return;
        }
        
        if (caption != null && !caption.trim().isEmpty()) {
            int length = caption.trim().length();
            
            if (config.getMinLength() != null && length < config.getMinLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video caption length " + length + " is less than minimum " + config.getMinLength())
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, "caption-min-length"))
                    .actualValue(length + " characters")
                    .expectedValue("minimum: " + config.getMinLength() + " characters")
                    .build());
            }
            
            if (config.getMaxLength() != null && length > config.getMaxLength()) {
                messages.add(ValidationMessage.builder()
                    .severity(severity)
                    .message("Video caption is too long")
                    .location(context.createLocation(block))
                    .ruleId(getRuleId(blockConfig, "caption-max-length"))
                    .actualValue(length + " characters")
                    .expectedValue("maximum: " + config.getMaxLength() + " characters")
                    .build());
            }
        }
    }
    
    private Integer parseInteger(Object value) {
        if (value == null) return null;
        
        try {
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return Integer.parseInt(value.toString().replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private String getRuleId(VideoBlock config, String ruleSuffix) {
        String baseName = config.getName() != null ? config.getName() : "video";
        return baseName + "-" + ruleSuffix;
    }
}