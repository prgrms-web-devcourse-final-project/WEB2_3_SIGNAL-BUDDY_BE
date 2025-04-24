package org.programmers.signalbuddyfinal.global.security;



import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetailsService;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationProviderTest {

    @InjectMocks
    private CustomAuthenticationProvider customAuthenticationProvider;

    @Mock
    private CustomUserDetailsService customUserDetailsService;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    private Member member;
    private CustomUserDetails customUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        member  = Member.builder()
            .memberId(1l)
            .email("test@test.com")
            .role(MemberRole.USER)
            .password(new BCryptPasswordEncoder().encode("password"))
            .memberStatus(MemberStatus.ACTIVITY)
            .build();

        customUserDetails = new CustomUserDetails(member);
        authentication = new UsernamePasswordAuthenticationToken(member.getEmail(), "password");
    }

    @DisplayName("해당하는 이메일이 DB에 존재하지 않아 에러가 발생한다.")
    @Test
    void givenNonExistentEmail_whenAuthenticate_thenThrowsNotFoundMemberError() {
        // given
        when(customUserDetailsService.loadUserByUsername(anyString())).thenThrow(
            UsernameNotFoundException.class);

        // when & then
        assertThatThrownBy(()->customAuthenticationProvider.authenticate(authentication))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("탈퇴한 회원의 접근으로 에러가 발생한다.")
    @Test
    void givenWithdrawalMember_whenAuthenticate_thenThrowsWithdrawnMemberError(){
        // given
        Member withdrawnMember = Member.builder()
            .memberId(1l)
            .email("test@test.com")
            .role(MemberRole.USER)
            .password(bCryptPasswordEncoder.encode("password"))
            .memberStatus(MemberStatus.WITHDRAWAL)
            .build();

        customUserDetails = new CustomUserDetails(withdrawnMember);
        authentication = new UsernamePasswordAuthenticationToken(withdrawnMember.getEmail(), "password");

        when(customUserDetailsService.loadUserByUsername(withdrawnMember.getEmail())).thenThrow(
            DisabledException.class);

        // when & then
        assertThatThrownBy(()->customAuthenticationProvider.authenticate(authentication))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(MemberErrorCode.WITHDRAWN_MEMBER.getMessage());
    }

    @DisplayName("비밀번호가 동일하지 않아 에러가 발생한다.")
    @Test
    void givenNotMatchedPassword_whenAuthenticate_thenThrowsNotFoundMemberError(){
        // given
        Authentication wrongAuthentication = new UsernamePasswordAuthenticationToken(member.getEmail(), "wrongPassword");
        when(customUserDetailsService.loadUserByUsername(anyString())).thenReturn(customUserDetails);
        when(bCryptPasswordEncoder.matches((String)wrongAuthentication.getCredentials(), customUserDetails.getPassword())).thenReturn(false);

        // when & then
        assertThatThrownBy(()->customAuthenticationProvider.authenticate(wrongAuthentication))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining(MemberErrorCode.NOT_FOUND_MEMBER.getMessage());
    }

    @DisplayName("성공적으로 authentication을 반환한다.")
    @Test
    void givenValidData_whenAuthenticate_thenReturnAuthentication(){
        // given
        when(customUserDetailsService.loadUserByUsername(member.getEmail())).thenReturn(customUserDetails);
        when(bCryptPasswordEncoder.matches((String)authentication.getCredentials(), customUserDetails.getPassword())).thenReturn(true);

        // when
        Authentication actualAuthentication = customAuthenticationProvider.authenticate(authentication);

        // then
        assertThat(((CustomUserDetails) actualAuthentication.getPrincipal()).getMemberId())
            .isEqualTo(customUserDetails.getMemberId());
        assertThat(actualAuthentication.getCredentials()).isNull();
        assertThat(actualAuthentication.getAuthorities())
            .extracting(GrantedAuthority::getAuthority)
            .containsExactly("ROLE_USER"); // or whatever your role is
    }
}
