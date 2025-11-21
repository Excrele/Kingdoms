package com.excrele.kingdoms.util;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Utility class for input validation
 */
public class InputValidator {
    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_\\-]{1,32}$");
    private static final Set<String> RESERVED_WORDS = new HashSet<>();
    
    static {
        RESERVED_WORDS.add("admin");
        RESERVED_WORDS.add("administrator");
        RESERVED_WORDS.add("server");
        RESERVED_WORDS.add("null");
        RESERVED_WORDS.add("none");
        RESERVED_WORDS.add("system");
    }
    
    /**
     * Validate kingdom name
     */
    public static ValidationResult validateKingdomName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationResult(false, "Kingdom name cannot be empty");
        }
        
        name = name.trim();
        
        if (name.length() < 3) {
            return new ValidationResult(false, "Kingdom name must be at least 3 characters");
        }
        
        if (name.length() > 32) {
            return new ValidationResult(false, "Kingdom name cannot exceed 32 characters");
        }
        
        if (!VALID_NAME_PATTERN.matcher(name).matches()) {
            return new ValidationResult(false, "Kingdom name can only contain letters, numbers, underscores, and hyphens");
        }
        
        if (RESERVED_WORDS.contains(name.toLowerCase())) {
            return new ValidationResult(false, "Kingdom name is reserved and cannot be used");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Validate economy amount
     */
    public static ValidationResult validateEconomyAmount(double amount) {
        if (amount < 0) {
            return new ValidationResult(false, "Amount cannot be negative");
        }
        
        if (amount > 1_000_000_000) {
            return new ValidationResult(false, "Amount exceeds maximum limit");
        }
        
        if (Double.isNaN(amount) || Double.isInfinite(amount)) {
            return new ValidationResult(false, "Invalid amount");
        }
        
        return new ValidationResult(true, null);
    }
    
    /**
     * Sanitize string input
     */
    public static String sanitize(String input) {
        if (input == null) return "";
        // Remove control characters and trim
        return input.replaceAll("[\\x00-\\x1F\\x7F]", "").trim();
    }
    
    /**
     * Validate coordinates
     */
    public static boolean isValidCoordinate(int coord) {
        return coord >= Integer.MIN_VALUE && coord <= Integer.MAX_VALUE;
    }
    
    /**
     * Validation result class
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;
        
        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }
        
        public boolean isValid() {
            return valid;
        }
        
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}

