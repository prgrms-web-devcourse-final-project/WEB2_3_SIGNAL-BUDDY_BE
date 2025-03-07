package org.programmers.signalbuddyfinal.domain.notification.dto;

import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FcmMessage {

    private final String title;

    private final String body;

    @Builder
    private FcmMessage(final String title, final String body) {
        this.title = Objects.requireNonNull(title);
        this.body = Objects.requireNonNull(body);
    }
}
