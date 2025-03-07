package org.programmers.signalbuddyfinal.domain.member.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.programmers.signalbuddyfinal.domain.social.entity.Provider;

@Setter
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinRequest {

    private Provider provider;

    private String SocialUserId;

    @Email(message = "이메일 형식에 맞지 않습니다.")
    private String email;

    @NotBlank(message = "닉네임은 필수 입력 사항입니다.")
    private String nickname;

    @NotBlank(message = "비밀번호는 필수 입력 사항입니다.")
    private String password;
}
