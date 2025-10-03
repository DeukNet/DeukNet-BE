package org.example.deuknetdomain.model.command.post.postcategory;

import java.util.UUID;
import org.example.deuknetdomain.model.command.post.post.Post;

public class PostCategoryAssignment {

    private final UUID id;
    private final Post post;
    private final UUID categoryId;

    private PostCategoryAssignment(UUID id, Post post, UUID categoryId) {
        this.id = id;
        this.post = post;
        this.categoryId = categoryId;
    }

    public static PostCategoryAssignment create(Post post, UUID categoryId) {
        return new PostCategoryAssignment(UUID.randomUUID(), post, categoryId);
    }

    public static PostCategoryAssignment restore(UUID id, Post post, UUID categoryId) {
        return new PostCategoryAssignment(id, post, categoryId);
    }

    public UUID getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public UUID getCategoryId() {
        return categoryId;
    }
}
