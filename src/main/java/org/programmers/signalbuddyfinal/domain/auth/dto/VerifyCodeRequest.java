package org.programmers.signalbuddyfinal.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class VerifyCodeRequest {

    private Purpose purpose;

    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 사항입니다.")
    private String email;

    @NotBlank(message = "인증 코드를 입력해주세요.")
    private String code;
}
