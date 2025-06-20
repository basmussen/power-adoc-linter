package com.example.linter.validator.rules;

import com.example.linter.validator.ValidationMessage;
import com.example.linter.validator.SourceLocation;
import java.util.List;

public interface AttributeRule {
    
    String getRuleId();
    
    List<ValidationMessage> validate(String attributeName, String value, SourceLocation location);
    
    boolean isApplicable(String attributeName);
}