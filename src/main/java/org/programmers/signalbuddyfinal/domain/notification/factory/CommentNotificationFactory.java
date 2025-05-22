package org.programmers.signalbuddyfinal.domain.notification.factory;

import java.util.Map;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage.Notification;
import org.springframework.stereotype.Component;

@Component
public class CommentNotificationFactory {

    public FcmMessage createMessage(
        String commentWriterNickname, String feedbackSubject,
        Long feedbackId
    ) {
        return FcmMessage.builder()
            .notification(
                Notification.builder()
                    .title("\uD83D\uDEA6 [" + commentWriterNickname + "]님이 당신의 피드백에 답변을 남겼어요!")
                    .body("\"" + feedbackSubject + "\"에 [" + commentWriterNickname + "]님의 의견이 추가되었습니다. 확인해 보시겠어요?")
                    .build()
            )
            .data(Map.of("feedbackId", feedbackId.toString()))
            .build();
    }
}
