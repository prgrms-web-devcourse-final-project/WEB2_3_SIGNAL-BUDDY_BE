package org.programmers.signalbuddyfinal.global.security.basic;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.member.entity.Member;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.domain.member.exception.MemberErrorCode;
import org.programmers.signalbuddyfinal.domain.member.repository.MemberRepository;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomUserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {

        Member findMember = identifier.contains("@")
            ? memberRepository.findByEmail(identifier).orElseThrow(()-> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER))
            : memberRepository.findById(Long.parseLong(identifier)).orElseThrow(()-> new BusinessException(MemberErrorCode.NOT_FOUND_MEMBER));
        final MemberStatus memberStatus = findMember.getMemberStatus();
        if (memberStatus == MemberStatus.WITHDRAWAL) {
            throw new BusinessException(MemberErrorCode.WITHDRAWN_MEMBER);
        }

        return new CustomUserDetails(findMember.getMemberId(), findMember.getEmail(),
            findMember.getPassword(), findMember.getProfileImageUrl(), findMember.getNickname(),
            findMember.getRole(), findMember.getMemberStatus());
    }
}
