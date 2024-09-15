package com.home.quartzapp.security.repository;

import com.home.quartzapp.security.entity.LoginUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LoginUserRepository extends JpaRepository<LoginUser, String>, LoginUserRepositoryCustom {
  Optional<LoginUser> findByUserId(String userId);
  Optional<LoginUser> findByLoginId(String loginId);
}