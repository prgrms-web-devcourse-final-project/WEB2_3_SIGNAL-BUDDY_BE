package org.programmers.signalbuddyfinal.domain.notification.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage;
import org.programmers.signalbuddyfinal.domain.notification.entity.FcmToken;
import org.programmers.signalbuddyfinal.domain.notification.exception.FcmErrorCode;
import org.programmers.signalbuddyfinal.domain.notification.repository.FcmTokenRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FcmService {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmTokenRepository fcmTokenRepository;
    private final MemberRepository memberRepository;

    @Async("customTaskExecutor")
    public void sendMessage(FcmMessage request, Long receiverId) {
        Optional<FcmToken> fcmToken = fcmTokenRepository.findByMemberId(receiverId);

        if (fcmToken.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getBody())
            .build();

        Message message = Message.builder()
            .setToken(fcmToken.get().getDeviceToken())
            .setNotification(notification)
            .build();

        try {
            String notiId = firebaseMessaging.send(message);
            log.info("notification ID : {}", notiId);
        } catch (FirebaseMessagingException e) {
            throw new BusinessException(FcmErrorCode.FCM_SEND_ERROR);
        }
    }

    @Transactional
    public void registerToken(String deviceToken, CustomUser2Member user) {
        Optional<FcmToken> entity = fcmTokenRepository.findByMemberId(user.getMemberId());

        if (entity.isPresent()) {
            throw new BusinessException(FcmErrorCode.ALREADY_EXISTED_TOKEN);
        }

        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        FcmToken fcmToken = FcmToken.create()
            .deviceToken(deviceToken).member(member)
            .build();

        fcmTokenRepository.save(fcmToken);
    }

    @Transactional
    public void updateToken(String deviceToken, CustomUser2Member user) {
        FcmToken fcmToken = fcmTokenRepository.findByMemberIdOrThrow(user.getMemberId());
        fcmToken.updateDeviceToken(deviceToken);
    }

    @Transactional
    public void deleteToken(CustomUser2Member user) {
        FcmToken fcmToken = fcmTokenRepository.findByMemberIdOrThrow(user.getMemberId());
        fcmTokenRepository.delete(fcmToken);
    }
}
