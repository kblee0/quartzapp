package com.home.quartzapp.security.service;

import com.home.quartzapp.security.entity.LoginUser;
import com.home.quartzapp.security.repository.LoginUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {
    private final LoginUserRepository loginUserRepository;

    @Override
    public LoginUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<LoginUser> loginUser = loginUserRepository.findByLoginId(username);

        return loginUser.map(LoginUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("LoginId not found ".concat(username)));
    }
    public LoginUserDetails loadUserByUserId(String userId) throws UsernameNotFoundException {

        Optional<LoginUser> loginUser = loginUserRepository.findByUserId(userId);

        return loginUser.map(LoginUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("UserId not found ".concat(userId)));
    }
}
