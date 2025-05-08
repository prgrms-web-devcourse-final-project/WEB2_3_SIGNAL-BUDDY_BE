package org.programmers.signalbuddyfinal.domain.auth.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailParseException;
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

    @Async
    public void sendEmail(String email) {

        MimeMessage message = javaMailSender.createMimeMessage();
        String code = createCode();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("[signalBuddy] 인증코드가 발송되었습니다.");
            helper.setText(setContent(code), true);
            javaMailSender.send(message);
            codeSave(email, code);
        } catch (MailParseException | MessagingException e) {
            log.error("메세지가 전송되지 않았습니다.");
        }
    }

    private String createCode() {

        SecureRandom secureRandom = new SecureRandom();
        int authenticationCode = secureRandom.nextInt((int) Math.pow(10, 6));
        log.info("authentication code: {}", authenticationCode);
        return String.format("%06d", authenticationCode);
    }

    private String setContent(String code) {

        Context context = new Context();
        context.setVariable("code", code);

        String content = templateEngine.process("member/mail", context);

        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException("Generated content is empty");
        }

        return content;
    }

    private void codeSave(String email, String code) {

        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(PREFIX + email, code, 3, TimeUnit.MINUTES);
    }
}
