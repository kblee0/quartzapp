package com.home.quartzapp.security.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.home.quartzapp.security.entity.QLoginUser.loginUser;

@Repository
@RequiredArgsConstructor
public class LoginUserRepositoryCustomImpl implements LoginUserRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;
    @Override
    public long updateRefreshTokenByUserId(String userId, String refreshToken) {
        return jpaQueryFactory.update(loginUser)
                .set(loginUser.refreshToken, refreshToken)
                .where(loginUser.userId.eq(userId))
                .execute();
    }
}
