package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface LoginUserRepository {
    public Optional<LoginUser> getLoginUser(@Param("loginId") String loginId);
}
