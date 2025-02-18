package org.programmers.signalbuddyfinal.global.security.oauth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberRole;
import org.programmers.signalbuddyfinal.domain.member.entity.enums.MemberStatus;
import org.programmers.signalbuddyfinal.global.security.oauth.response.OAuth2Response;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

@Getter
@AllArgsConstructor
public class CustomOAuth2User implements OAuth2User, Serializable {

    private OAuth2Response oAuth2Response;
    private Long memberId;
    private String email;
    private String password;
    private String profileImageUrl;
    private String nickname;
    private MemberRole role;
    private MemberStatus status;

    @Override
    public String getName() {
        return oAuth2Response.getName();
    }

    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                return "ROLE_" + role.name();
            }
        });

        return authorities;
    }
}
