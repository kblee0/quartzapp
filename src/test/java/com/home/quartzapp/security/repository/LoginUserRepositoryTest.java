package com.home.quartzapp.security.repository;

import com.home.quartzapp.common.config.QuerydslConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@Import({QuerydslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginUserRepositoryTest {
    @Autowired
    private LoginUserRepository loginUserRepository;
    @Test
    void findByLoginId() {
        var loginUser = loginUserRepository.findByLoginId("admin");
        log.info("admin loingUser: {}", loginUser.get().getLoginId());
        assertThat(loginUser.get().getLoginId()).isEqualTo("admin");
    }
}