package org.programmers.signalbuddyfinal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpHeaders;

@Getter
@AllArgsConstructor
public class LogoutResponse {

    private HttpHeaders httpHeaders;
}
