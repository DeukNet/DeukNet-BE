package org.example.deuknetdomain.common.vo;

import org.example.deuknetdomain.common.exception.InvalidValueException;

public class CategoryName {
    
    private static final int MAX_LENGTH = 50;
    private final String value;

    private CategoryName(String value) {
        validate(value);
        this.value = value;
    }

    public static CategoryName of(String value) {
        return new CategoryName(value.trim());
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Category name cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Category name cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String getValue() {
        return value;
    }
}
