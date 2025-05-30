package org.programmers.signalbuddyfinal.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.core.ApiFuture;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.fixture.TestMemberFactory;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage.Notification;
import org.programmers.signalbuddyfinal.domain.notification.entity.FcmToken;
import org.programmers.signalbuddyfinal.domain.notification.exception.FcmErrorCode;
import org.programmers.signalbuddyfinal.domain.notification.repository.FcmTokenRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.IntegrationTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FcmServiceTest extends IntegrationTest {

    @Autowired
    private FcmService fcmService;

    @MockitoBean
    private FirebaseMessaging firebaseMessaging;

    @Autowired
    private FcmTokenRepository fcmTokenRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Mock
    private ApiFuture<BatchResponse> apiFuture;

    private Member member;
    private FcmToken fcmToken;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
            TestMemberFactory.createActiveUser("test email", "tester")
        );
        fcmToken = saveFcmToken("test token", member);
    }

    @DisplayName("알림 메시지를 보낸다.")
    @Test
    void sendMessage_Success() {
        // Given
        FcmMessage request = getFcmMessage("test title", "test body");
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        when(firebaseMessaging.sendEachForMulticastAsync(any(MulticastMessage.class)))
            .thenReturn(apiFuture);

        // When
        fcmService.sendMessage(request, user.getMemberId());

        // Then
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(firebaseMessaging, times(1))
                    .sendEachForMulticastAsync(any(MulticastMessage.class))
            );
    }

    @DisplayName("알림 메시지를 보내려고 하지만 사용자가 디바이스 토큰 등록을 하지 않아 알림 전송이 되지 않는다.")
    @Test
    void sendMessageNotDeviceToken_Success() {
        // Given
        FcmMessage request = getFcmMessage("test title", "test body");
        Member otherMember = memberRepository.save(
            TestMemberFactory.createActiveUser("test1 email", "other tester")
        );
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId());

        when(firebaseMessaging.sendEachForMulticastAsync(any(MulticastMessage.class)))
            .thenReturn(apiFuture);

        // When
        fcmService.sendMessage(request, user.getMemberId());

        // Then
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(firebaseMessaging, times(0))
                    .sendEachForMulticastAsync(any(MulticastMessage.class))
            );
    }

    @DisplayName("알림 메시지를 보내는 데 실패한다.")
    @Test
    void sendMessage_Failure() {
        // Given
        FcmMessage request = getFcmMessage("test title", "test body");
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        when(firebaseMessaging.sendEachForMulticastAsync(any(MulticastMessage.class)))
            .thenReturn(apiFuture);
        when(apiFuture.isCancelled()).thenReturn(true);

        // When & Then
        try {
            fcmService.sendMessage(request, user.getMemberId());
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FcmErrorCode.FCM_SEND_ERROR);
        }
    }

    @DisplayName("사용자가 디바이스 토큰을 추가로 등록한다.")
    @Test
    void registerToken_Success() {
        // Given
        String deviceToken = "test Token2";
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        // When
        HttpCookie result = fcmService.registerToken(deviceToken, user);

        // Then
        assertThat(fcmTokenRepository.findByIdOrThrow(result.getValue()).getDeviceToken())
            .isEqualTo(deviceToken);
    }

    @DisplayName("사용자가 로그인하여 디바이스 토큰을 활성화한다.")
    @Test
    void loginToken() {
        // Given
        fcmToken.logout();

        // When
        fcmService.loginToken(fcmToken.getFcmTokenUuid());

        // Then
        assertThat(fcmTokenRepository.findAllByMemberId(member.getMemberId())).isNotEmpty();
    }

    @DisplayName("사용자가 로그아웃하여 디바이스 토큰을 비활성화한다.")
    @Test
    void logoutToken() {
        // When
        fcmService.logoutToken(fcmToken.getFcmTokenUuid());

        // Then
        assertThat(fcmTokenRepository.findAllByMemberId(member.getMemberId())).isEmpty();
    }

    @DisplayName("해당 디바이스 토큰 DB와 쿠키에서 삭제한다.")
    @Test
    void deleteDeviceToken_Success() {
        // Given
        String deviceTokenUuid = fcmToken.getFcmTokenUuid();
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        // When
        ResponseCookie result = fcmService.deleteDeviceToken(deviceTokenUuid, user);

        // Then
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(fcmTokenRepository.findAllByMemberId(member.getMemberId()))
                .isEmpty();
            softAssertions.assertThat(result.getValue()).isEqualTo("");
            softAssertions.assertThat(result.getName()).isEqualTo("device-token");
            softAssertions.assertThat(result.getMaxAge()).isZero();
            softAssertions.assertThat(result.getPath()).isEqualTo("/");
        });
    }

    @DisplayName("디바이스 토큰 소유자와 삭제 요청자가 다를 경우 실패한다.")
    @Test
    void deleteDeviceToken_Failure() {
        // Given
        String deviceTokenUuid = fcmToken.getFcmTokenUuid();
        Member otherMember = memberRepository.save(
            TestMemberFactory.createActiveUser("other email", "other")
        );
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId());

        // When & Then
        try {
            fcmService.deleteDeviceToken(deviceTokenUuid, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode())
                .isEqualTo(FcmErrorCode.FCM_TOKEN_ELIMINATOR_NOT_AUTHORIZED);
        }
    }

    private FcmToken saveFcmToken(String deviceToken, Member member) {
        return fcmTokenRepository.save(
            FcmToken.create().deviceToken(deviceToken).member(member).build());
    }

    private CustomUser2Member getCurrentMember(Long id) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));
    }

    private FcmMessage getFcmMessage(String title, String body) {
        return FcmMessage.builder()
            .notification(
                Notification.builder()
                    .title(title).body(body)
                    .build()
            )
            .build();
    }
}