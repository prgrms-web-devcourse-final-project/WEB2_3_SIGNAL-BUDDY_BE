package org.programmers.signalbuddyfinal.global.anotation;

import java.util.Collection;
import java.util.List;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.dto.CustomUser2Member;
import org.programmers.signalbuddyfinal.global.security.basic.CustomUserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

// 테스트용 SecurityContext
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        final SecurityContext securityContext = SecurityContextHolder.createEmptyContext();

        // 커스텀한 Mock User를 SecurityContext에 추가
        final CustomUser2Member user = new CustomUser2Member(
            new CustomUserDetails(
                Long.parseLong(annotation.userName()), "", "", "",
                "", MemberRole.USER, MemberStatus.ACTIVITY
            )
        );
        final Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(annotation.roleType()));
        final Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);
        securityContext.setAuthentication(authentication);

        return securityContext;
    }
}
