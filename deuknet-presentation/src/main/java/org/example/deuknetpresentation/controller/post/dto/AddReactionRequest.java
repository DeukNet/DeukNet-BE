package org.example.deuknetpresentation.controller.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "리액션 추가 요청")
public class AddReactionRequest {
    
    @Schema(
            description = "리액션 타입", 
            example = "LIKE",
            allowableValues = {"LIKE", "LOVE", "HAHA", "WOW", "SAD", "ANGRY"},
            required = true
    )
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
