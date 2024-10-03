package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;

import java.util.Optional;

public interface LoginUserRepositoryCustom {
    long updateRefreshTokenByUserId(String userId, String refreshToken);
    Optional<LoginUser> findByUserId(String userId);
    Optional<LoginUser> findByLoginId(String loginId);
}
