package org.example.deuknetdomain.common.vo;

import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public final class Email {

    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private final String value;

    private Email(String value) {
        this.value = value;
    }

    public static Email of(String value) {
        validate(value);
        return new Email(value.toLowerCase());
    }

    public static Email from(String value) {
        return of(value);
    }

    private static void validate(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!VALID_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Email email)) return false;
        return Objects.equals(value, email.value);
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
