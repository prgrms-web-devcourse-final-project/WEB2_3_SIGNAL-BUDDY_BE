package org.programmers.signalbuddyfinal.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmRequest;
import org.programmers.signalbuddyfinal.domain.notification.exception.FcmErrorCode;
import org.programmers.signalbuddyfinal.domain.notification.repository.FcmTokenRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;

    public void sendMessage(FcmRequest request) {
        Notification notification = Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getBody())
            .build();

        Message message = Message.builder()
            .setToken(request.getDeviceToken())
            .setNotification(notification)
            .build();

        try {
            String response = firebaseMessaging.send(message);
            log.info("FCM Response ID : {}", response);
        } catch (FirebaseMessagingException e) {
            throw new BusinessException(FcmErrorCode.FCM_SEND_ERROR);
        }
    }
}
