package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.GetCommentsUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 댓글 조회 서비스
 *
 * 책임:
 * - 특정 게시글의 댓글 목록 조회
 * - 작성자 정보와 함께 조회 (N+1 방지)
 */
@Service
@Transactional(readOnly = true)
public class GetCommentsService implements GetCommentsUseCase {

    private final CommentRepository commentRepository;

    public GetCommentsService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    @Override
    public List<CommentProjection> getCommentsByPostId(UUID postId) {
        return commentRepository.findProjectionsByPostId(postId);
    }
}
