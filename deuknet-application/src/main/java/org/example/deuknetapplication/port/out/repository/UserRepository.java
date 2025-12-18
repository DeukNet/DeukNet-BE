package org.example.deuknetapplication.port.out.repository;

import org.example.deuknetdomain.domain.post.AuthorType;
import org.example.deuknetdomain.domain.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    Optional<User> findByAuthCredentialId(UUID authCredentialId);
    Optional<User> findById(UUID id);
    Optional<User> findByUsername(String username);
    Page<User> findAll(Pageable pageable);
    Page<User> searchByKeyword(String keyword, Pageable pageable);

    /**
     * Post용 User 정보 enrichment
     * ANONYMOUS인 경우: authorId 유지 (프론트엔드에서 isAuthor 체크용), "익명"으로 표시
     * REAL인 경우: PostgreSQL에서 User 조회하여 실제 정보 설정
     *
     * @param response 작성자 정보를 설정할 응답 객체
     */
    void enrichWithUserInfo(AuthorInfoEnrichable response);

    /**
     * Comment용 User 정보 enrichment
     * ANONYMOUS인 경우: authorId를 null로 설정하고 "익명"으로 표시
     * REAL인 경우: PostgreSQL에서 User 조회하여 실제 정보 설정
     *
     * @param response 작성자 정보를 설정할 응답 객체
     */
    void enrichWithUserInfoForComment(AuthorInfoEnrichable response);
}
