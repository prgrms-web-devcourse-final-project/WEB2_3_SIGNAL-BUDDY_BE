package org.programmers.signalbuddyfinal.global.security.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private MemberRepository memberRepository;

    private Member savedMember;

    @BeforeEach
    void setUp() {
        savedMember = Member.builder()
            .memberId(1l)
            .email("test@test.com")
            .nickname("테스트용 사용자")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.ACTIVITY)
            .build();
    }

    @DisplayName("email을 통해 DB를 조회하고, CustomUserDetail을 반환한다.")
    @Test
    void givenEmail_whenLoadByUsername_thenReturnCustomUserDetails(){
        // given
        when(memberRepository.findByEmail(savedMember.getEmail())).thenReturn(Optional.of(savedMember));

        // when
        CustomUserDetails actualCustomUserDetails = customUserDetailsService.loadUserByUsername("test@test.com");

        // then
        assertThat(actualCustomUserDetails).isNotNull();
        assertThat(actualCustomUserDetails.getMemberId()).isEqualTo(savedMember.getMemberId());
        assertThat(actualCustomUserDetails.getEmail()).isEqualTo(savedMember.getEmail());
        assertThat(actualCustomUserDetails.getNickname()).isEqualTo(savedMember.getNickname());
        assertThat(actualCustomUserDetails.getRole()).isEqualTo(savedMember.getRole());
    }

    @DisplayName("memberId를 통해 DB를 조회하고, CustomUserDetail을 반환한다.")
    @Test
    void givenMemberId_whenLoadUserByUsername_thenReturnCustomUserDetail(){
        // given
        when(memberRepository.findById(savedMember.getMemberId())).thenReturn(Optional.of(savedMember));

        // when
        CustomUserDetails actualCustomUserDetails = customUserDetailsService.loadUserByUsername("1");

        // then
        assertThat(actualCustomUserDetails).isNotNull();
        assertThat(actualCustomUserDetails.getMemberId()).isEqualTo(savedMember.getMemberId());
        assertThat(actualCustomUserDetails.getEmail()).isEqualTo(savedMember.getEmail());
        assertThat(actualCustomUserDetails.getNickname()).isEqualTo(savedMember.getNickname());
        assertThat(actualCustomUserDetails.getRole()).isEqualTo(savedMember.getRole());
    }

    @DisplayName("DB에 존재하지 않아 에러가 발생한다.")
    @Test
    void givenNonExistentMemberId_whenLoadUserByUsername_thenThrowsNotFoundMemberError(){
        // given
        when(memberRepository.findById(2l)).thenThrow(new UsernameNotFoundException(MemberErrorCode.NOT_FOUND_MEMBER.getMessage()));

        // when & then
        assertThatThrownBy(()->customUserDetailsService.loadUserByUsername("2"))
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("탈퇴한 회원으로 에러가 발생한다.")
    @Test
    void givenWithdrawalMember_whenLoadUserByUsername_thenThrowsWithdrawnMemberError(){
        // given
        Member withdrawalMember = Member.builder()
            .memberId(2l)
            .nickname("탈퇴한 사용자")
            .role(MemberRole.USER)
            .memberStatus(MemberStatus.WITHDRAWAL)
            .build();

        when(memberRepository.findById(2l)).thenReturn(Optional.of(withdrawalMember));

        // when & then
        assertThatThrownBy(()->customUserDetailsService.loadUserByUsername("2"))
            .isInstanceOf(DisabledException.class)
            .hasMessageContaining(MemberErrorCode.WITHDRAWN_MEMBER.getMessage());
    }
}
