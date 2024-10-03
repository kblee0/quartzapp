package com.home.quartzapp.security.repository;

import com.home.quartzapp.common.config.QuerydslConfig;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@Slf4j
@DataJpaTest
@Import({QuerydslConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginUserRepositoryCustomImplTest {

    @Autowired
    private LoginUserRepositoryCustomImpl loginUserRepositoryCustomImpl;

    @Test
    void findByLoginId() {
        var loginUser = loginUserRepositoryCustomImpl.findByLoginId("admin");
        log.info("admin loingUser: {}", loginUser.get().getLoginId());
        Assertions.assertThat(loginUser.get().getLoginId()).isEqualTo("admin");
    }
}