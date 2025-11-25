package org.example.deuknetapplication.port.out.external.search;

import org.example.deuknetapplication.projection.post.PostDetailProjection;

public interface PostProjectionCommandPort {

    void indexPostDetail(String payloadJson);

    void updatePostCounts(String payloadJson);

    void deletePost(String postId);

    void save(PostDetailProjection projection);
}
