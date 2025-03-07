package org.programmers.signalbuddyfinal.domain.notification.service;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import java.util.List;
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
        List<FcmToken> fcmTokens = fcmTokenRepository.findAllByMemberId(receiverId);
        log.info("fcmToken size : {}", fcmTokens.size());

        if (fcmTokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
            .setTitle(request.getTitle())
            .setBody(request.getBody())
            .build();

        List<String> deviceTokens = fcmTokens.stream()
            .map(FcmToken::getDeviceToken).toList();

        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(notification)
            .build();

        ApiFuture<BatchResponse> apiFuture = firebaseMessaging.sendEachForMulticastAsync(message);
        if (apiFuture.isCancelled()) {
            throw new BusinessException(FcmErrorCode.FCM_SEND_ERROR);
        }
    }

    @Transactional
    public void registerToken(String deviceToken, CustomUser2Member user) {
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        FcmToken fcmToken = FcmToken.create()
            .deviceToken(deviceToken).member(member)
            .build();

        fcmTokenRepository.save(fcmToken);
    }
}
