package org.programmers.signalbuddyfinal.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;

@Getter
@AllArgsConstructor
public class SocialLoginRequest {

    private Provider provider;
    private String socialUserId;
}
