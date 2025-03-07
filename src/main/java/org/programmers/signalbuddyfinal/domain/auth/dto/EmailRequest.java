package org.programmers.signalbuddyfinal.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EmailRequest {

    @Email(message = "이메일 형식에 맞지 않습니다.")
    @NotBlank(message = "이메일은 필수 입력 사항입니다.")
    private String email;
}
