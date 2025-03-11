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
import org.springframework.http.ResponseCookie;
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

    private static final String DEVICE_TOKEN_COOKIE_NAME = "device-token";

    @Transactional
    @Async("customTaskExecutor")
    public void sendMessage(FcmMessage request, Long receiverId) {
        List<FcmToken> fcmTokens = fcmTokenRepository.findAllByMemberId(receiverId);
        log.info("fcmToken size : {}", fcmTokens.size());

        if (fcmTokens.isEmpty()) {
            return;
        }

        Notification notification = Notification.builder()
            .setTitle(request.getNotification().getTitle())
            .setBody(request.getNotification().getBody())
            .build();

        List<String> deviceTokens = fcmTokens.stream()
            .map(FcmToken::getDeviceToken).toList();

        MulticastMessage message = MulticastMessage.builder()
            .addAllTokens(deviceTokens)
            .setNotification(notification)
            .putAllData(request.getData())
            .build();

        ApiFuture<BatchResponse> apiFuture = firebaseMessaging.sendEachForMulticastAsync(message);
        if (apiFuture.isCancelled()) {
            throw new BusinessException(FcmErrorCode.FCM_SEND_ERROR);
        }

        for (FcmToken fcmToken : fcmTokens) {
            fcmToken.updateLastUsedAt();
        }
    }

    @Transactional
    public ResponseCookie registerToken(String deviceToken, CustomUser2Member user) {
        Member member = memberRepository.findByIdOrThrow(user.getMemberId());

        FcmToken fcmToken = FcmToken.create()
            .deviceToken(deviceToken).member(member)
            .build();
        fcmTokenRepository.save(fcmToken);

        return makeCookieByDeviceToken(fcmToken.getFcmTokenUuid());
    }

    @Transactional
    public void loginToken(String fcmTokenUuid) {
        if (fcmTokenUuid == null || fcmTokenUuid.isBlank()) {
            return;
        }

        FcmToken fcmToken = fcmTokenRepository.findByIdOrThrow(fcmTokenUuid);
        fcmToken.login();
    }

    @Transactional
    public void logoutToken(String fcmTokenUuid) {
        if (fcmTokenUuid == null || fcmTokenUuid.isBlank()) {
            return;
        }

        FcmToken fcmToken = fcmTokenRepository.findByIdOrThrow(fcmTokenUuid);
        fcmToken.logout();
    }

    @Transactional
    public ResponseCookie deleteDeviceToken(String deviceTokenUuid, CustomUser2Member user) {
        FcmToken fcmToken = fcmTokenRepository.findByIdOrThrow(deviceTokenUuid);

        if (Member.isNotSameMember(user, fcmToken.getMember())) {
            throw new BusinessException(FcmErrorCode.FCM_TOKEN_ELIMINATOR_NOT_AUTHORIZED);
        }

        fcmTokenRepository.delete(fcmToken);

        return ResponseCookie.from(DEVICE_TOKEN_COOKIE_NAME, "")
            .maxAge(0).path("/").build();
    }

    private ResponseCookie makeCookieByDeviceToken(String deviceTokenUuid) {
        return ResponseCookie
            .from(DEVICE_TOKEN_COOKIE_NAME, deviceTokenUuid)
            .httpOnly(true)
            .secure(true)
            .path("/")
            .sameSite("None")
            .build();
    }
}
