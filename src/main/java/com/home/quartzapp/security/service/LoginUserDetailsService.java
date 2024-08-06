package com.home.quartzapp.security.service;

import com.home.quartzapp.security.entity.LoginUser;
import com.home.quartzapp.security.repository.LoginUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LoginUserDetailsService implements UserDetailsService {
    private final LoginUserRepository loginUserRepository;

    @Override
    public LoginUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<LoginUser> userDetail = loginUserRepository.getLoginUser(username);

        // Converting userDetail to UserDetails
        return userDetail.map(LoginUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found ".concat(username)));
    }
}
