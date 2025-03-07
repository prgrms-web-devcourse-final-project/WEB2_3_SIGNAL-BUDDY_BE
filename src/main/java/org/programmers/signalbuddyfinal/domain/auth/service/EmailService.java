package org.programmers.signalbuddyfinal.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.programmers.signalbuddyfinal.domain.auth.dto.EmailRequest;
import org.programmers.signalbuddyfinal.domain.auth.dto.VerifyCodeRequest;
import org.programmers.signalbuddyfinal.domain.auth.entity.Purpose;
import org.programmers.signalbuddyfinal.domain.auth.exception.AuthErrorCode;
import org.programmers.signalbuddyfinal.global.exception.BusinessException;
import org.programmers.signalbuddyfinal.global.response.ApiResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    static final String PREFIX = "auth:email:";

    // 이메일 발송
    @Async
    public void sendEmail(EmailRequest emailRequest) {

        MimeMessage message = javaMailSender.createMimeMessage();
        String code = createCode();

        try {
            // 이메일 내용 기입
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(emailRequest.getEmail());
            helper.setSubject("[signalBuddy] 인증코드가 발송되었습니다.");
            helper.setText(setContent(code), true);
        } catch (MessagingException e) {
            throw new BusinessException(AuthErrorCode.SEND_EMAIL_FAILED);
        }

        // 인증 코드 저장
        codeSave(emailRequest.getEmail(), code);

        // 이메일 발송
        javaMailSender.send(message);
    }

    // 인증 코드 검증
    public ResponseEntity<ApiResponse<Object>> verifyCode(VerifyCodeRequest verifyCodeRequest) {

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Purpose purpose = verifyCodeRequest.getPurpose();
        String email = verifyCodeRequest.getEmail();
        String code = verifyCodeRequest.getCode();

        String correctCode = valueOperations.get(PREFIX + email);

        if (correctCode == null) {
            throw new BusinessException(AuthErrorCode.INVALID_AUTH_CODE);
        } else if (!correctCode.equals(code)) {
            throw new BusinessException(AuthErrorCode.NOT_MATCH_AUTH_CODE);
        } else {
            redisTemplate.delete(PREFIX + email);

            // 인증된 사용자 저장
            String newPrefix = PREFIX + purpose.name().toLowerCase() + ":";
            valueOperations.set(newPrefix + email, "authenticated", 10,
                TimeUnit.MINUTES);
            return ResponseEntity.ok().body(ApiResponse.createSuccessWithNoData());
        }
    }

    // 인증 코드 생성
    private String createCode() {

        SecureRandom secureRandom = new SecureRandom();
        int authenticationCode = secureRandom.nextInt((int) Math.pow(10, 6));
        log.info("authentication code: {}", authenticationCode);
        return String.format("%06d", authenticationCode);
    }

    // 이메일 내용 작성
    private String setContent(String code) {

        Context context = new Context();
        context.setVariable("code", code);

        String content = templateEngine.process("member/mail", context);

        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Generated content is empty");
        }

        return content;
    }

    // 인증 코드 저장
    private void codeSave(String email, String code) {

        // 이미 요청한 메일에 대한 인증코드가 존재하는 경우, 삭제한다.
        if (Boolean.TRUE.equals(redisTemplate.hasKey(PREFIX + email))) {
            redisTemplate.delete(PREFIX + email);
        }

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(PREFIX + email, code, 3, TimeUnit.MINUTES);
    }
}
