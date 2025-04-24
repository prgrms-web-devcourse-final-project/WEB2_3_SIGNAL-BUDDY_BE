package org.programmers.signalbuddyfinal.domain.auth.dto;


import lombok.Builder;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.springframework.http.HttpHeaders;

@Builder
@Getter
public class LoginResponse {

    private HttpHeaders httpHeaders;
    private MemberResponse memberResponse;
    private String message;

    public static LoginResponse success(HttpHeaders httpHeaders, MemberResponse memberResponse) {
        return new LoginResponse(httpHeaders, memberResponse, null);
    }

    public static LoginResponse fail(String message) {
        return new LoginResponse(null, null, message);
    }
}
