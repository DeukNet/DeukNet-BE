package org.example.deuknetpresentation.controller.post.dto;

public class AddReactionRequest {
    private String reactionType;

    public AddReactionRequest() {
    }

    public AddReactionRequest(String reactionType) {
        this.reactionType = reactionType;
    }

    public String getReactionType() {
        return reactionType;
    }

    public void setReactionType(String reactionType) {
        this.reactionType = reactionType;
    }
}
