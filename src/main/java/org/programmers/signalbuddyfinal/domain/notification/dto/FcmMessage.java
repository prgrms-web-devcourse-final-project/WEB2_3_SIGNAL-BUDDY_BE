package org.programmers.signalbuddyfinal.domain.notification.dto;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import lombok.Builder;
import lombok.Getter;

@Getter
public class FcmMessage {

    private final Notification notification;

    private final Map<String, String> data;

    @Builder
    private FcmMessage(final Notification notification, final Map<String, String> data) {
        this.notification = Objects.requireNonNull(notification);

        if (data == null) {
            this.data = Collections.emptyMap();
        } else {
            this.data = Collections.unmodifiableMap(data);
        }
    }

    @Getter
    public static class Notification {

        private final String title;

        private final String body;

        @Builder
        private Notification(final String title, final String body) {
            this.title = Objects.requireNonNull(title);
            this.body = Objects.requireNonNull(body);
        }
    }
}
