package org.example.deuknetapplication.port.in.user;

/**
 * 사용자 프로필 업데이트 Use Case
 * <br>
 * 현재 사용자 정보는 Service에서 CurrentUserPort를 통해 조회합니다.
 */
public interface UpdateUserProfileUseCase {
    /**
     * 현재 사용자의 프로필 업데이트
     *
     * @param command 프로필 업데이트 명령
     */
    void updateProfile(UpdateUserProfileCommand command);
}
