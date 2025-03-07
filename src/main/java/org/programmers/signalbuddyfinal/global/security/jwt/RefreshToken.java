package org.programmers.signalbuddyfinal.global.security.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString
public class RefreshToken {

    private String memberId;
    private String refreshTokenValue;
}
