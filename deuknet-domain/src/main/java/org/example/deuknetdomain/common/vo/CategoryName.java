package org.example.deuknetdomain.common.vo;

import java.util.Objects;

public final class CategoryName {
    private static final int MAX_LENGTH = 100;
    private final String value;

    private CategoryName(String value) {
        this.value = value;
    }

    public static CategoryName of(String value) {
        validate(value);
        return new CategoryName(value.trim());
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Category name cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CategoryName that)) return false;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
