package org.example.deuknetapplication.port.in.comment;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class CreateCommentApplicationRequest {
    private UUID postId;
    private String content;
    private UUID parentCommentId;

    protected CreateCommentApplicationRequest() {
    }

    public CreateCommentApplicationRequest(UUID postId, String content, UUID parentCommentId) {
        this.postId = postId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

}
