package com.mustapha.ecommerce.shared.security;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Service;

@Service
public class InputSanitizationService {
    
    public String sanitizeForHtml(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtml(input);
    }
    
    public String sanitizeForHtmlAttribute(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forHtmlAttribute(input);
    }
    
    public String sanitizeForJavaScript(String input) {
        if (input == null) {
            return null;
        }
        return Encode.forJavaScript(input);
    }
    
    public String sanitizeForJson(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("\"", "\\\"")
                   .replace("\\", "\\\\")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }
    
    public String sanitizeForSql(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("'", "''")
                   .replace("\\", "\\\\")
                   .replace("\0", "");
    }
    
    public String sanitizeUserInput(String input) {
        if (input == null) {
            return null;
        }
        
        String sanitized = input.trim();
        
        sanitized = sanitized.replaceAll("<script[^>]*>.*?</script>", "");
        sanitized = sanitized.replaceAll("<iframe[^>]*>.*?</iframe>", "");
        sanitized = sanitized.replaceAll("javascript:", "");
        sanitized = sanitized.replaceAll("on\\w+\\s*=", "");
        
        return Encode.forHtml(sanitized);
    }
    
    public String sanitizeEmail(String email) {
        if (email == null) {
            return null;
        }
        
        String sanitized = email.trim().toLowerCase();
        
        if (!sanitized.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new IllegalArgumentException("Invalid email format");
        }
        
        return sanitized;
    }
    
    public String sanitizeNumericString(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replaceAll("[^0-9]", "");
    }
    
    public String sanitizeAlphanumeric(String input) {
        if (input == null) {
            return null;
        }
        
        return input.replaceAll("[^a-zA-Z0-9]", "");
    }
}
