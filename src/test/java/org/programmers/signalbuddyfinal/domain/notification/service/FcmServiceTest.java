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
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.notification.dto.FcmMessage;
import org.programmers.signalbuddyfinal.domain.notification.entity.FcmToken;
import org.programmers.signalbuddyfinal.domain.notification.exception.FcmErrorCode;
import org.programmers.signalbuddyfinal.domain.notification.repository.FcmTokenRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class FcmServiceTest extends ServiceTest {

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

    @BeforeEach
    void setUp() {
        member = saveMember("test email", "tester");
        saveFcmToken("test token1", member);
    }

    @DisplayName("알림 메시지를 보낸다.")
    @Test
    void sendMessage_Success() {
        // Given
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
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
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
        Member otherMember = saveMember("test1 email", "other tester");
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
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
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
        String deviceToken = "test Token";
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        // When
        fcmService.registerToken(deviceToken, user);

        // Then
        List<FcmToken> actual = fcmTokenRepository.findAllByMemberId(user.getMemberId());
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.size()).isEqualTo(2);
            softAssertions.assertThat(actual.get(1).getDeviceToken()).isEqualTo(deviceToken);
        });
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
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
}