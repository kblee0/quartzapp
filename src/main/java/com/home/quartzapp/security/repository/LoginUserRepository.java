package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface LoginUserRepository {
    Optional<LoginUser> findByLoginId(@Param("loginId") String loginId);
    Optional<LoginUser> findByUserId(@Param("userId") String Id);
    int updateRefreshTokenByUserId(@Param("userId") String loginId, @Param("refreshToken") String refreshToken);
}
