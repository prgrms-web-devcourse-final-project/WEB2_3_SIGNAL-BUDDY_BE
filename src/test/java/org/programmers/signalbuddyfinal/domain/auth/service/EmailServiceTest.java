package org.programmers.signalbuddyfinal.domain.auth.service;


import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.programmers.signalbuddyfinal.global.db.RedisTestContainer;
import org.programmers.signalbuddyfinal.global.support.ServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.spring6.SpringTemplateEngine;

@EnableAsync
@Transactional
class EmailServiceTest extends ServiceTest implements RedisTestContainer {

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    SpringTemplateEngine springTemplateEngine;

    final String PREFIX = "auth:email:";

    @Test
    @DisplayName("이메일 전송에 성공한다.")
    void givenValidEmail_whenSendEmail_thenSendEmail() {
        // when
        emailService.sendEmail("test@test.com");

        // then
        await().atMost(4, TimeUnit.SECONDS)
            .untilAsserted(() -> {
                assertTrue(redisTemplate.hasKey(PREFIX + "test@test.com"));
            });
    }

}
