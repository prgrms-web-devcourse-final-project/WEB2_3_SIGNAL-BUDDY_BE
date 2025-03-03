package org.programmers.signalbuddyfinal.domain.social.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.Getter;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.http.converter.HttpMessageNotReadableException;

@Getter
public enum Provider {
    KAKAO("kakao"),
    NAVER("naver"),
    GOOGLE("google");

    private final String value;

    Provider(String value) {
        this.value = value;
    }

    @JsonCreator
    public static Provider Json2Enum(String provider) {
        for(Provider p : values()) {
            if(p.value.equalsIgnoreCase(provider)) {
                return p;
            }
        }
        throw new IllegalArgumentException();
    }
}
