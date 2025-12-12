package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.CommentResponse;
import org.example.deuknetapplication.port.in.comment.GetCommentsUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetapplication.projection.comment.CommentProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.example.deuknetdomain.domain.post.AuthorType.ANONYMOUS;

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
    private final UserRepository userRepository;
    private final CurrentUserPort currentUserPort;

    public GetCommentsService(
            CommentRepository commentRepository,
            UserRepository userRepository,
            CurrentUserPort currentUserPort
    ) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
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
     * 익명 여부에 따라 작성자 정보를 설정하고, 현재 사용자의 작성자 여부를 응답에 추가
     * - 현재 사용자가 작성자인지 먼저 확인 (enrichWithUserInfo 전에 authorId 체크 필요)
     * - UserRepository.enrichWithUserInfo를 통해 익명 처리
     * - 인증되지 않은 사용자의 경우 isAuthor는 false로 설정
     *
     * @param response 응답 객체
     */
    private void enrichWithUserInfo(CommentResponse response) {
        // 1. 현재 사용자가 작성자인지 먼저 확인 (enrichWithUserInfoForComment가 authorId를 null로 만들기 전에)
        boolean isAuthor;
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            isAuthor = response.getAuthorId() != null && response.getAuthorId().equals(currentUserId);
        } catch (Exception e) {
            // 인증되지 않은 사용자
            isAuthor = false;
        }

        // 2. Comment용 enrichment (익명이면 authorId를 null로 설정)
        userRepository.enrichWithUserInfoForComment(response);

        // 3. isAuthor 설정
        response.setIsAuthor(isAuthor);
    }
}
