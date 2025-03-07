package org.programmers.signalbuddyfinal.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;
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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberJoinRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberNotiAllowRequest;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberResponse;
import org.programmers.signalbuddyfinal.domain.member.dto.MemberUpdateRequest;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.service.AwsFileService;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    private final Long id = 1L;
    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AwsFileService awsFileService;

    @InjectMocks
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        member = Member.builder().memberId(id).email("test@example.com").password("password123")
            .nickname("TestUser").profileImageUrl("http://example.com/profile.jpg").notifyEnabled(null)
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

    @Test
    @DisplayName("회원 가입 성공")
    void savedMember() {

        MockMultipartFile profileImage = new MockMultipartFile("profileImage", "", "image/jpeg",
            new byte[0]);
        ReflectionTestUtils.setField(memberService, "defaultProfileImage", "test-path");
        ReflectionTestUtils.setField(memberService, "memberDir", "test-dir");

        URL mockURL = mock(URL.class);
        when(awsFileService.uploadFileToS3(any(MockMultipartFile.class), anyString())).thenReturn(profileImage.getName());
        when(awsFileService.getFileFromS3(anyString(), anyString())).thenReturn(mockURL);

        //given
        final MemberJoinRequest request = MemberJoinRequest.builder().email("test2@example.com")
            .nickname("TestUser2").password("password123").build();
        final Member expectedMember = Member.builder().memberId(id).email("test2@example.com")
            .nickname("TestUser2").profileImageUrl(mockURL.toString()).memberStatus(MemberStatus.ACTIVITY)
            .role(MemberRole.USER).build();

        when(memberRepository.save(any(Member.class))).thenReturn(expectedMember);

        //when
        MemberResponse actualResponse = memberService.joinMember(request, profileImage);

        //then
        assertThat(actualResponse.getEmail()).isEqualTo(expectedMember.getEmail());
        assertThat(actualResponse.getNickname()).isEqualTo(expectedMember.getNickname());
        assertThat(actualResponse.getProfileImageUrl()).isEqualTo(
            expectedMember.getProfileImageUrl());
        assertThat(actualResponse.getMemberStatus()).isEqualTo(expectedMember.getMemberStatus());
        assertThat(actualResponse.getRole()).isEqualTo(expectedMember.getRole());

        verify(memberRepository, times(1)).save(any(Member.class));
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