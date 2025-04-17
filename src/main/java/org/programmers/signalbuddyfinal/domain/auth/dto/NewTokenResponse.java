package org.programmers.signalbuddyfinal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewTokenResponse {

    private String accessToken;
    private String refreshToken;
}
