package com.home.quartzapp.security.service;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.JwtTokenDto;
import com.home.quartzapp.security.dto.LoginRequestDto;
import com.home.quartzapp.security.dto.LoginResponseDto;
import com.home.quartzapp.security.entity.LoginUser;
import com.home.quartzapp.security.repository.LoginUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class LoginUserService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginUserRepository loginUserRepository;

    public LoginResponseDto userLogin(LoginRequestDto loginRequestDto) {
        Authentication authentication;
        UsernamePasswordAuthenticationToken authenticationToken;

        authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());

        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        }
        catch (BadCredentialsException | UsernameNotFoundException e) {
            throw ApiException.code("SCR0001");
        }
        catch (Exception e) {
            throw ApiException.code("CMNE0001");
        }

        if(!authentication.isAuthenticated()) throw ApiException.code("SCR0001");

        return createJwtToken(loginRequestDto.getLoginId());
    }

    public LoginResponseDto refreshLogin(LoginRequestDto loginRequestDto) {
        Optional<String> refreshToken = Optional.ofNullable(loginRequestDto.getRefreshToken());

        if(!jwtService.validateToken(refreshToken.orElse(""))) {
            throw ApiException.code("SCR0004");
        }

        Optional<LoginUser> loginUser = loginUserRepository.getLoginUser(loginRequestDto.getLoginId());

        if(!loginUser.map(LoginUser::getRefreshToken).equals(refreshToken)) {
            throw ApiException.code("SCR0005");
        }

        return createJwtToken(loginRequestDto.getLoginId());
    }

    private LoginResponseDto createJwtToken(String loginId) {
        JwtTokenDto jwtTokenDto = jwtService.generateToken(loginId);

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(jwtTokenDto.getAccessToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .expiresIn(jwtTokenDto.getExpiresIn())
                .tokenType(jwtTokenDto.getTokenType())
                .build();

        loginUserRepository.updateLoginUserRefreshToken(loginId, jwtTokenDto.getRefreshToken());

        return loginResponseDto;
    }
}
