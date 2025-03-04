package org.programmers.signalbuddyfinal.domain.member.exception;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.ErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MemberErrorCode implements ErrorCode {

    NOT_FOUND_MEMBER(HttpStatus.NOT_FOUND, "06000", "해당 사용자를 찾을 수 없습니다."),
    ALREADY_EXIST_EMAIL(HttpStatus.CONFLICT, "06001", "이미 존재하는 이메일 입니다."),
    WITHDRAWN_MEMBER(HttpStatus.FORBIDDEN, "06002", "탈퇴한 회원입니다."),
    ALREADY_EXIST_NICKNAME(HttpStatus.CONFLICT, "06003", "이미 존재하는 닉네임 입니다."),
    ALREADY_EXIST_SOCIAL_ACCOUNT(HttpStatus.CONFLICT, "06004", "해당 소셜 계정은 이미 가입 되어있는 계정입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
