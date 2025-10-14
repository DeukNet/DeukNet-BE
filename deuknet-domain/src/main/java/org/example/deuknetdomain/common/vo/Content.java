package org.example.deuknetdomain.common.vo;

import org.example.deuknetdomain.common.exception.InvalidValueException;

import java.util.Objects;
import lombok.Getter;

@Getter
public final class Content {
    private static final int MAX_LENGTH = 10000;
    private final String value;

    private Content(String value) {
        this.value = value;
    }

    public static Content of(String value) {
        validate(value);
        return new Content(value.trim());
    }

    public static Content from(String value) {
        return of(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Content cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Content cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Content content)) return false;
        return Objects.equals(value, content.value);
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
