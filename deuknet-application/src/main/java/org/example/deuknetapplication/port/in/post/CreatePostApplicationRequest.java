package org.example.deuknetapplication.port.in.post;

import lombok.Getter;
import lombok.Setter;
import org.example.deuknetdomain.domain.post.AuthorType;

import java.util.UUID;

@Setter
@Getter
public class CreatePostApplicationRequest {
    private String title;
    private String content;
    private UUID categoryId;
    private AuthorType authorType;

    protected CreatePostApplicationRequest() {
    }

    public CreatePostApplicationRequest(String title, String content, UUID categoryId, AuthorType authorType) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
        this.authorType = authorType;
    }
}
