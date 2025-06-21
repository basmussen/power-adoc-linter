package com.example.linter.validator.rules;

import java.util.List;

import com.example.linter.validator.SourceLocation;
import com.example.linter.validator.ValidationMessage;

public interface AttributeRule {
    
    String getRuleId();
    
    List<ValidationMessage> validate(String attributeName, String value, SourceLocation location);
    
    boolean isApplicable(String attributeName);
}