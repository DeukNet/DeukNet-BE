package org.example.deuknetdomain.common.vo;

import lombok.Getter;
import org.example.deuknetdomain.common.exception.InvalidValueException;
import org.example.deuknetdomain.common.seedwork.ValueObject;

import java.util.Objects;

@Getter
public class CategoryName extends ValueObject {

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
}
