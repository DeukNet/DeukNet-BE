package org.example.deuknetapplication.port.in.permission;

import lombok.Getter;

/**
 * 익명 권한 신청 커맨드
 */
@Getter
public class RequestAnonymousAccessCommand {

    private final String password;

    public RequestAnonymousAccessCommand(String password) {
        this.password = password;
    }
}
