package org.example.deuknetapplication.service.comment;

import org.example.deuknetapplication.port.in.comment.CommentResponse;
import org.example.deuknetapplication.port.in.comment.GetCommentsUseCase;
import org.example.deuknetapplication.port.out.repository.CommentRepository;
import org.example.deuknetapplication.port.out.repository.UserRepository;
import org.example.deuknetapplication.port.out.security.CurrentUserPort;
import org.example.deuknetdomain.domain.comment.Comment;
import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;
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
 * - Domain 객체를 Response DTO로 변환
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
        List<Comment> comments = commentRepository.findByPostId(postId);

        // 익명 조회 권한이 없으면 익명 댓글 필터링
        boolean canAccessAnonymous = hasAnonymousAccessPermission();

        return comments.stream()
                .filter(comment -> canAccessAnonymous || !AuthorType.ANONYMOUS.equals(comment.getAuthorType()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 현재 사용자의 익명 조회 권한 확인
     */
    private boolean hasAnonymousAccessPermission() {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            User user = userRepository.findById(currentUserId).orElse(null);
            return user != null && user.isCanAccessAnonymous();
        } catch (Exception e) {
            // 비인증 사용자는 익명 조회 불가
            return false;
        }
    }

    private CommentResponse toResponse(Comment comment) {
        // 1. 현재 사용자가 작성자인지 확인
        boolean isAuthor = isCurrentUserAuthor(comment);

        // 2. 익명 여부에 따라 정적 팩토리 메서드 사용
        if (comment.getAuthorType() == AuthorType.ANONYMOUS) {
            return CommentResponse.fromAnonymous(comment, isAuthor);
        } else {
            User author = userRepository.findById(comment.getAuthorId()).orElse(null);
            return CommentResponse.from(comment, author, isAuthor);
        }
    }

    private boolean isCurrentUserAuthor(Comment comment) {
        try {
            UUID currentUserId = currentUserPort.getCurrentUserId();
            return comment.getAuthorId().equals(currentUserId);
        } catch (Exception e) {
            // 인증되지 않은 사용자
            return false;
        }
    }
}
