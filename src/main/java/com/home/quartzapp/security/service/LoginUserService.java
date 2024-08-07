package com.home.quartzapp.security.service;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.JwtTokenDto;
import com.home.quartzapp.security.dto.LoginRequestDto;
import com.home.quartzapp.security.dto.LoginResponseDto;
import com.home.quartzapp.security.entity.LoginUser;
import com.home.quartzapp.security.repository.LoginUserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.security.auth.login.AccountLockedException;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class LoginUserService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginUserRepository loginUserRepository;
    private final LoginUserDetailsService loginUserDetailsService;

    public LoginResponseDto userLogin(LoginRequestDto loginRequestDto) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginRequestDto.getLoginId(), loginRequestDto.getPassword());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        try {
            authenticationToken = (UsernamePasswordAuthenticationToken)authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e) {
            log.error("userLoing error :: loginId : {}, errorMessage: {}", loginRequestDto.getLoginId(), e.getMessage());
            ApiException apiException = switch (e) {
                case BadCredentialsException ignored -> ApiException.code("SCR0001");
                case UsernameNotFoundException ignored -> ApiException.code("SCR0001");
                case AccountLockedException ignored -> ApiException.code("SCR0006");
                case AccountExpiredException ignored -> ApiException.code("SCR0006");
                case DisabledException ignored -> ApiException.code("SCR0006");
                default -> ApiException.code("CMNE0001", e.getMessage());
            };
            throw apiException;
        }

        if(!authenticationToken.isAuthenticated()) throw ApiException.code("SCR0001");

        return createJwtToken(authenticationToken);
    }

    public LoginResponseDto refreshLogin(LoginRequestDto loginRequestDto) {
        Optional<String> refreshToken = Optional.ofNullable(loginRequestDto.getRefreshToken());

        // Validate refresh token
        if(!jwtService.validateToken(refreshToken.orElse(""))) {
            throw ApiException.code("SCR0004");
        }

        Optional<LoginUser> loginUser = loginUserRepository.findByLoginId(loginRequestDto.getLoginId());

        if(!loginUser.map(LoginUser::getRefreshToken).equals(refreshToken)) {
            throw ApiException.code("SCR0005");
        }

        // Get Authentication
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        LoginUserDetails loginUserDetails = loginUserDetailsService.loadUserByUsername(loginRequestDto.getLoginId());
        if(!loginUserDetails.isAccountNonExpired()||!loginUserDetails.isAccountNonLocked()||!loginUserDetails.isEnabled()) {
            throw ApiException.code("SCR0006");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUserDetails, null, loginUserDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return createJwtToken(authenticationToken);
    }

    private LoginResponseDto createJwtToken(Authentication authentication) {
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();

        JwtTokenDto jwtTokenDto = jwtService.generateToken(authentication);

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .accessToken(jwtTokenDto.getAccessToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .expiresIn(jwtTokenDto.getExpiresIn())
                .tokenType(jwtTokenDto.getTokenType())
                .build();

        loginUserRepository.updateRefreshTokenByUserId(loginUserDetails.getUserId(), jwtTokenDto.getRefreshToken());

        return loginResponseDto;
    }
}
