package org.programmers.signalbuddyfinal.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
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

    private Member member;

    @BeforeEach
    void setUp() {
        member = saveMember("test email", "tester");
        saveFcmToken(member);
    }

    @DisplayName("알림 메시지를 보낸다.")
    @Test
    void sendMessage_Success() throws FirebaseMessagingException {
        // Given
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        when(firebaseMessaging.send(any(Message.class))).thenReturn("mock_message_id");

        // When
        fcmService.sendMessage(request, user.getMemberId());

        // Then
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(firebaseMessaging, times(1)).send(any(Message.class))
            );
    }

    @DisplayName("알림 메시지를 보내려고 하지만 사용자가 디바이스 토큰 등록을 하지 않아 알림 전송이 되지 않는다.")
    @Test
    void sendMessageNotDeviceToken_Success() throws FirebaseMessagingException {
        // Given
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
        Member otherMember = saveMember("test1 email", "other tester");
        CustomUser2Member user = getCurrentMember(otherMember.getMemberId());

        when(firebaseMessaging.send(any(Message.class))).thenReturn("mock_message_id");

        // When
        fcmService.sendMessage(request, user.getMemberId());

        // Then
        await()
            .atMost(5, TimeUnit.SECONDS)
            .untilAsserted(() ->
                verify(firebaseMessaging, times(0)).send(any(Message.class))
            );
    }

    @DisplayName("알림 메시지를 보내는 데 실패한다.")
    @Test
    void sendMessage_Failure()
        throws FirebaseMessagingException {
        // Given
        FcmMessage request = FcmMessage.builder()
            .title("test title").body("test body")
            .build();
        CustomUser2Member user = getCurrentMember(member.getMemberId());
        Message message = Message.builder()
            .setToken("test token")
            .setNotification(Notification.builder()
                .setTitle(request.getTitle())
                .setBody(request.getBody())
                .build())
            .build();

        when(firebaseMessaging.send(message))
            .thenThrow(new BusinessException(FcmErrorCode.FCM_SEND_ERROR));

        // When & Then
        try {
            fcmService.sendMessage(request, user.getMemberId());
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FcmErrorCode.FCM_SEND_ERROR);
        }
    }

    @DisplayName("사용자가 디바이스 토큰을 처음 등록한다.")
    @Test
    void registerToken_Success() {
        // Given
        Member userMember = saveMember("test email2", "tester2");
        String deviceToken = "test Token";
        CustomUser2Member user = getCurrentMember(userMember.getMemberId());

        // When
        fcmService.registerToken(deviceToken, user);

        // Then
        FcmToken actual = fcmTokenRepository.findByMemberIdOrThrow(user.getMemberId());
        assertThat(actual.getDeviceToken()).isEqualTo(deviceToken);
    }

    @DisplayName("디바이스 추가 등록 시 실패한다.")
    @Test
    void registerToken_Failure() {
        // Given
        Member userMember = saveMember("test email2", "tester2");
        String deviceToken = "test Token";
        CustomUser2Member user = getCurrentMember(userMember.getMemberId());

        // When & Then
        fcmService.registerToken(deviceToken, user);
        try {
            fcmService.registerToken(deviceToken, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FcmErrorCode.ALREADY_EXISTED_TOKEN);
        }
    }

    @DisplayName("사용자가 디바이스 토큰을 업데이트한다.")
    @Test
    void updateToken_Success() {
        // Given
        String updatedDeviceToken = "test updated Token";
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        // When
        fcmService.updateToken(updatedDeviceToken, user);

        // Then
        FcmToken actual = fcmTokenRepository.findByMemberIdOrThrow(user.getMemberId());
        assertThat(actual.getDeviceToken()).isEqualTo(updatedDeviceToken);
    }

    @DisplayName("디바이스 토큰을 등록하지 않은 사용자가 토큰을 업데이트할 시 실패한다.")
    @Test
    void updateToken_Failure() {
        // Given
        Member userMember = saveMember("test email2", "tester2");
        String updatedDeviceToken = "test updated Token";
        CustomUser2Member user = getCurrentMember(userMember.getMemberId());

        // When & Then
        try {
            fcmService.updateToken(updatedDeviceToken, user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FcmErrorCode.FCM_TOKEN_NOT_FOUND);
        }
    }

    @DisplayName("사용자가 디바이스 토큰을 삭제한다.")
    @Test
    void deleteToken_Success() {
        // Given
        CustomUser2Member user = getCurrentMember(member.getMemberId());

        // When
        fcmService.deleteToken(user);

        // Then
        assertThat(fcmTokenRepository.findByMemberId(user.getMemberId())).isEmpty();
    }

    @DisplayName("본인 것이 아닌 토큰을 삭제할 시 실패한다.")
    @Test
    void deleteToken_Failure() {
        // Given
        Member userMember = saveMember("test email2", "tester2");
        CustomUser2Member user = getCurrentMember(userMember.getMemberId());

        // When & Then
        try {
            fcmService.deleteToken(user);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(FcmErrorCode.FCM_TOKEN_NOT_FOUND);
        }
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(
            Member.builder().email(email).password("123456").role(MemberRole.USER)
                .nickname(nickname).memberStatus(MemberStatus.ACTIVITY)
                .profileImageUrl("https://test-image.com/test-123131").build());
    }

    private FcmToken saveFcmToken(Member member) {
        return fcmTokenRepository.save(
            FcmToken.create().deviceToken("test token").member(member).build());
    }

    private CustomUser2Member getCurrentMember(Long id) {
        return new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));
    }
}