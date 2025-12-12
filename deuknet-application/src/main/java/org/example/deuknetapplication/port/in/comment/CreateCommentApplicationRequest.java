package org.example.deuknetapplication.port.in.comment;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.post.AuthorType;

import java.util.UUID;

@Setter
@Getter
public class CreateCommentApplicationRequest {
    private UUID postId;
    private String content;
    private UUID parentCommentId;
    private AuthorType authorType = AuthorType.REAL;

    protected CreateCommentApplicationRequest() {
    }

    public CreateCommentApplicationRequest(UUID postId, String content, UUID parentCommentId) {
        this.postId = postId;
        this.content = content;
        this.parentCommentId = parentCommentId;
    }

    public CreateCommentApplicationRequest(UUID postId, String content, UUID parentCommentId, AuthorType authorType) {
        this.postId = postId;
        this.content = content;
        this.parentCommentId = parentCommentId;
        this.authorType = authorType;
    }

}
