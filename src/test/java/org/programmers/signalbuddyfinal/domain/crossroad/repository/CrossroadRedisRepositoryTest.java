package org.programmers.signalbuddyfinal.domain.crossroad.repository;

import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.programmers.signalbuddyfinal.global.config.RedisConfig;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest
@Import(RedisConfig.class)
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CrossroadRedisRepositoryTest {


}
