package org.example.deuknetdomain.common.vo;

import java.util.Objects;

public final class Title {
    private static final int MAX_LENGTH = 200;
    private final String value;

    private Title(String value) {
        this.value = value;
    }

    public static Title of(String value) {
        validate(value);
        return new Title(value.trim());
    }

    public static Title from(String value) {
        return of(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new IllegalArgumentException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Title title)) return false;
        return Objects.equals(value, title.value);
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
