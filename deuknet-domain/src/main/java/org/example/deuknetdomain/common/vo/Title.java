package org.example.deuknetdomain.common.vo;

import lombok.Getter;
import org.example.deuknetdomain.common.exception.InvalidValueException;

@Getter
public class Title {
    private static final int MAX_LENGTH = 200;
    private final String value;

    private Title(String value) {
        validate(value);
        this.value = value;
    }

    public static Title of(String value) {
        return of(value);
    }

    public static Title from(String value) {
        return of(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidValueException("Title cannot be empty");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException("Title cannot exceed " + MAX_LENGTH + " characters");
        }
    }
}
