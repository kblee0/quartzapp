package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.spel.ast.Projection;
import org.springframework.stereotype.Repository;

import java.util.Optional;

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

    @Override
    public Optional<LoginUser> findByUserId(String userId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.bean(LoginUser.class, loginUser.userId, loginUser.loginId, loginUser.email, loginUser.name, loginUser.roles))
                .from(loginUser)
                .where(loginUser.userId.eq(userId))
                .fetchOne());
    }

    @Override
    public Optional<LoginUser> findByLoginId(String loginId) {
        return Optional.ofNullable(jpaQueryFactory
                .select(Projections.bean(LoginUser.class, loginUser.userId, loginUser.loginId, loginUser.email, loginUser.name, loginUser.roles))
                .from(loginUser)
                .where(loginUser.loginId.eq(loginId))
                .fetchOne());
    }
}
