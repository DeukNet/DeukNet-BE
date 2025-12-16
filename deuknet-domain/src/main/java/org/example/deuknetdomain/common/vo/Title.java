package org.example.deuknetdomain.common.vo;

import lombok.Getter;
import org.example.deuknetdomain.common.exception.InvalidValueException;
import org.example.deuknetdomain.common.seedwork.ValueObject;

import java.util.Objects;

@Getter
public class Title extends ValueObject {
    private static final int MAX_LENGTH = 200;
    private final String value;

    private Title(String value) {
        validate(value);
        this.value = value;
    }

    public static Title of(String value) {
        return new Title(value);
    }

    public static Title from(String value) {
        return new Title(value);
    }

    @Override
    protected void validate() {
        validate(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Title cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
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
}
