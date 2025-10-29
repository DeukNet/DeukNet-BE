package org.example.deuknetinfrastructure.external.oauth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GithubEmailResponse {

    @JsonProperty("email")
    private String email;

    @JsonProperty("primary")
    private Boolean primary;

    @JsonProperty("verified")
    private Boolean verified;

    public GithubEmailResponse() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getPrimary() {
        return primary;
    }

    public void setPrimary(Boolean primary) {
        this.primary = primary;
    }

    public Boolean getVerified() {
        return verified;
    }

    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
}
