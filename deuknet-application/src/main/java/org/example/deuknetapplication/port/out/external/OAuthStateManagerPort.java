package org.example.deuknetapplication.port.out.external;

public interface OAuthStateManagerPort {
    String generateState();
    void validateState(String state);
}
