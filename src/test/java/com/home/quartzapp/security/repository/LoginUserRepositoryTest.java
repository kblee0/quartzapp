package com.home.quartzapp.security.repository;

import com.home.quartzapp.common.config.QueryDSLConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@Import({QueryDSLConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@ActiveProfiles("local")
class LoginUserRepositoryTest {
    @Autowired
    private LoginUserRepository loginUserRepository;
    @Test
    void findByUserId() {
        var loginUser = loginUserRepository.findByLoginId("admin");
        log.info("admin loingUser: {}", loginUser.get().getLoginId());
        assertThat(loginUser.get().getLoginId()).isEqualTo("admin");
    }
}