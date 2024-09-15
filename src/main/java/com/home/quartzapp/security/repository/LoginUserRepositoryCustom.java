package com.home.quartzapp.security.repository;

public interface LoginUserRepositoryCustom {
    long updateRefreshTokenByUserId(String userId, String refreshToken);
}
