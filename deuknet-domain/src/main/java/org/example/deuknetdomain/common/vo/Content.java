package org.example.deuknetdomain.common.vo;

import java.util.Objects;

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

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Content cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Content cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String getValue() {
        return value;
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
