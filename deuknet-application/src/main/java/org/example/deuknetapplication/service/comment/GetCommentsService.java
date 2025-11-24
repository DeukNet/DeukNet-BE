package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.CommentResponse;
import org.example.deuknetapplication.port.in.comment.GetCommentsUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 댓글 조회 서비스
 *
 * 책임:
 * - 특정 게시글의 댓글 목록 조회
 * - 작성자 정보와 함께 조회 (N+1 방지)
 * - 현재 사용자의 작성자 여부 확인
 */
@Service
@Transactional(readOnly = true)
public class GetCommentsService implements GetCommentsUseCase {

    private final CommentRepository commentRepository;
    private final CurrentUserPort currentUserPort;

    public GetCommentsService(
            CommentRepository commentRepository,
            CurrentUserPort currentUserPort
    ) {
        this.commentRepository = commentRepository;
        this.currentUserPort = currentUserPort;
    }

    @Override
    public List<CommentResponse> getCommentsByPostId(UUID postId) {
        List<CommentProjection> projections = commentRepository.findProjectionsByPostId(postId);

        return projections.stream()
                .map(projection -> {
                    CommentResponse response = new CommentResponse(projection);
                    enrichWithUserInfo(response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    /**
     * 현재 사용자의 작성자 여부를 응답에 추가
     * 인증되지 않은 사용자의 경우 false로 설정
     *
     * @param response 응답 객체
     */
    private void enrichWithUserInfo(CommentResponse response) {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            response.setIsAuthor(response.getAuthorId().equals(currentUserId));
        } catch (Exception e) {
            // 인증되지 않은 사용자
            response.setIsAuthor(false);
        }
    }
}
