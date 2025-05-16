package org.programmers.signalbuddyfinal.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberNotiAllowRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;
import org.programmers.signalbuddyfinal.domain.social.entity.SocialProvider;
import org.programmers.signalbuddyfinal.domain.social.repository.SocialProviderRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

class MemberServiceTest extends ServiceTest {

    private final Long id = 1L;
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private SocialProviderRepository socialProviderRepository;

    @Mock
    private AwsFileService awsFileService;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder().memberId(id).email("test@example.com").password("password123")
            .nickname("TestUser").profileImageUrl("http://example.com/profile.jpg")
            .notifyEnabled(null)
            .role(MemberRole.USER).memberStatus(MemberStatus.ACTIVITY).build();
    }

    @Test
    @DisplayName("계정 조회 테스트")
    void getMember() {
        MemberResponse expectedResponse = MemberResponse.builder().memberId(id)
            .email("test@example.com").nickname("TestUser")
            .profileImageUrl("http://example.com/profile.jpg").role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY).build();

        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        MemberResponse actualResponse = memberService.getMember(id);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(memberRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("계정 수정 테스트")
    void updateMember() {
        final MemberUpdateRequest updateRequest = MemberUpdateRequest.builder()
            .email("test2@example.com").nickname("TestUser2").password("password123").build();

        final MemberResponse expectedResponse = MemberResponse.builder().memberId(id)
            .email("test2@example.com").nickname("TestUser2").memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.USER).build();

        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        final MemberResponse actualResponse = memberService.updateMember(id, updateRequest
        );

        assertThat(actualResponse.getEmail()).isEqualTo(expectedResponse.getEmail());
        assertThat(actualResponse.getNickname()).isEqualTo(expectedResponse.getNickname());
        verify(memberRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("계정 탈퇴 테스트")
    void deleteMember() {
        final MemberStatus expected = MemberStatus.WITHDRAWAL;
        when(memberRepository.findById(id)).thenReturn(Optional.of(member));

        memberService.deleteMember(id);
        assertThat(member.getMemberStatus()).isEqualTo(expected);
        verify(memberRepository, times(1)).findById(id);
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class JoinTest {

        private MockMultipartFile profileImage;
        private URL profileImageUrl;
        private MemberJoinRequest memberBasicJoinRequest;
        private MemberJoinRequest memberSocialJoinRequest;

        @BeforeEach
        void setUp() {
            memberBasicJoinRequest = MemberJoinRequest.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .password(member.getPassword())
                .build();

            memberSocialJoinRequest = MemberJoinRequest.builder()
                .provider(Provider.GOOGLE)
                .socialUserId("socialUUID")
                .email(member.getEmail())
                .password(member.getPassword())
                .nickname(member.getNickname())
                .build();

            ReflectionTestUtils.setField(memberService, "defaultProfileImage", "test-path");
            ReflectionTestUtils.setField(memberService, "memberDir", "test-dir");
            profileImage = new MockMultipartFile("profileImage", "empty.jpg", "image/jpeg",
                new byte[0]);
            profileImageUrl = mock(URL.class);
            when(awsFileService.uploadFileToS3(any(MockMultipartFile.class),
                anyString())).thenReturn(profileImage.getOriginalFilename());
            when(awsFileService.getFileFromS3(anyString(), anyString())).thenReturn(
                profileImageUrl);
        }

        @Nested
        @DisplayName("기본 회원가입")
        class whenBasicJoin {

            @Test
            @DisplayName("기본 회원가입에 성공한다.")
            void givenRightRequest_whenBasicJoin_thenSuccess() {
                // given
                when(memberRepository.save(any(Member.class))).thenReturn(member);

                //when
                MemberResponse actualResponse = memberService.joinMember(memberBasicJoinRequest,
                    profileImage);

                //then
                assertThat(actualResponse.getEmail()).isEqualTo(member.getEmail());
                assertThat(actualResponse.getNickname()).isEqualTo(member.getNickname());
                assertThat(actualResponse.getProfileImageUrl()).isEqualTo(
                    member.getProfileImageUrl());
                assertThat(actualResponse.getMemberStatus()).isEqualTo(member.getMemberStatus());
                assertThat(actualResponse.getRole()).isEqualTo(member.getRole());

                verify(memberRepository, times(1)).save(any(Member.class));
            }

            @Test
            @DisplayName("이미 존재하는 이메일로 회원가입을 요청하는 경우, 실패한다.")
            void givenAlreadyExistEmail_whenBasicJoin_thenThrowsAlreadyExistEmailError() {
                // given
                when(memberRepository.findByEmail(memberBasicJoinRequest.getEmail())).thenReturn(
                    Optional.of(member));
                // when & then
                assertThatThrownBy(
                    () -> memberService.joinMember(memberBasicJoinRequest, profileImage))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(
                        MemberErrorCode.ALREADY_EXIST_EMAIL.getMessage());
            }

            @Test
            @DisplayName("이미 존재하는 닉네임으로 회원가입을 요청하는 경우, 실패한다.")
            void givenAlreadyNickname_whenBasicJoin_thenThrowsAlreadyExistNicknameError() {
                // given
                when(memberRepository.existsByNickname(
                    memberBasicJoinRequest.getNickname())).thenReturn(true);
                // when & then
                assertThatThrownBy(
                    () -> memberService.joinMember(memberBasicJoinRequest, profileImage))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(
                        MemberErrorCode.ALREADY_EXIST_NICKNAME.getMessage());
            }
        }

        @Nested
        @DisplayName("소셜 회원가입")
        class SocialJoin {

            @Test
            @DisplayName("소셜 회원가입에 성공한다.")
            void givenRightRequest_whenSocialJoin_thenSuccess() {
                // given
                SocialProvider socialProvider = SocialProvider.builder()
                    .socialId("testSocialId")
                    .socialProviderId(1l)
                    .oauthProvider(Provider.GOOGLE)
                    .member(member)
                    .build();
                when(memberRepository.save(any(Member.class))).thenReturn(member);
                when(socialProviderRepository.save(any(SocialProvider.class))).thenReturn(
                    socialProvider);

                // when
                MemberResponse actualResponse = memberService.joinMember(memberSocialJoinRequest,
                    profileImage);

                // then
                assertThat(actualResponse.getEmail()).isEqualTo(member.getEmail());
                assertThat(actualResponse.getNickname()).isEqualTo(member.getNickname());
                assertThat(actualResponse.getProfileImageUrl()).isEqualTo(
                    member.getProfileImageUrl());
                assertThat(actualResponse.getMemberStatus()).isEqualTo(member.getMemberStatus());
                assertThat(actualResponse.getRole()).isEqualTo(member.getRole());

                verify(memberRepository, times(1)).save(any(Member.class));
                verify(socialProviderRepository, times(1)).save(any(SocialProvider.class));
            }

            @Test
            @DisplayName("이미 소셜 회원가입이 되어 있는 경우, 실패한다.")
            void givenAlreadyExistSocialData_whenSocialJoin_thenThrowsAlreadyExistSocialAccountError() {
                // given
                when(socialProviderRepository.existsByOauthProviderAndSocialId(
                    memberSocialJoinRequest.getProvider(),
                    memberSocialJoinRequest.getSocialUserId())).thenReturn(true);
                // when & then
                assertThatThrownBy(
                    () -> memberService.joinMember(memberSocialJoinRequest, profileImage))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(
                        MemberErrorCode.ALREADY_EXIST_SOCIAL_ACCOUNT.getMessage());
            }

            @Test
            @DisplayName("기본 이메일로 소셜 회원가입 시, 기본 계정과 소셜 계정이 연동된다.")
            void givenExistingEmail_whenSocialJoin_thenLinkWithBasicAccount() {
                // given
                when(memberRepository.findByEmail(memberSocialJoinRequest.getEmail())).thenReturn(
                    Optional.of(member));
                // when
                memberService.joinMember(memberSocialJoinRequest, profileImage);
                // then
                verify(socialProviderRepository, times(1)).save(any(SocialProvider.class));
            }
        }
    }

    @Test
    @DisplayName("사용자의 알림 허용 설정을 변경한다.")
    void updateNotifyEnabled_Success() {
        // Given
        MemberNotiAllowRequest request = new MemberNotiAllowRequest(Boolean.FALSE);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(id, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        when(memberRepository.findByIdOrThrow(id)).thenReturn(member);

        // When
        memberService.updateNotifyEnabled(id, user, request);

        // Then
        assertThat(member.getNotifyEnabled()).isFalse();
    }

    @Test
    @DisplayName("Path Variable 값과 요청자가 다를 경우 실패한다.")
    void updateNotifyEnabled_Failure() {
        // Given
        MemberNotiAllowRequest request = new MemberNotiAllowRequest(Boolean.FALSE);
        CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(9999L, "", "",
                "", "", MemberRole.USER, MemberStatus.ACTIVITY));

        when(memberRepository.findByIdOrThrow(id)).thenReturn(member);

        // When & Then
        try {
            memberService.updateNotifyEnabled(id, user, request);
        } catch (BusinessException e) {
            assertThat(e.getErrorCode()).isEqualTo(MemberErrorCode.REQUESTER_IS_NOT_SAME);
        }
    }
}