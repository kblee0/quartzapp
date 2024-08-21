package com.home.quartzapp.security.service;

import com.home.quartzapp.common.exception.ApiException;
import com.home.quartzapp.security.dto.JwtTokenDto;
import com.home.quartzapp.security.dto.AuthTokenRequestDto;
import com.home.quartzapp.security.dto.AuthTokenResponseDto;
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
public class AuthTokenService {
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginUserRepository loginUserRepository;
    private final LoginUserDetailsService loginUserDetailsService;

    public AuthTokenResponseDto authTokenByPassword(AuthTokenRequestDto authTokenRequestDto) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authTokenRequestDto.getLoginId(), authTokenRequestDto.getPassword());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        try {
            authenticationToken = (UsernamePasswordAuthenticationToken)authenticationManager.authenticate(authenticationToken);
        }
        catch (Exception e) {
            log.error("authTokenByPassword error :: loginId : {}", authTokenRequestDto.getLoginId());
            throw switch (e) {
                case BadCredentialsException cause -> ApiException.code("SCR0001", cause);
                case UsernameNotFoundException cause -> ApiException.code("SCR0001", cause);
                case AccountLockedException cause -> ApiException.code("SCR0006", cause);
                case AccountExpiredException cause -> ApiException.code("SCR0006", cause);
                case DisabledException cause -> ApiException.code("SCR0006", cause);
                default -> ApiException.code("CMNE0001", e, e.getMessage());
            };
        }

        if(!authenticationToken.isAuthenticated()) throw ApiException.code("SCR0001");

        return createJwtToken(authenticationToken);
    }

    public AuthTokenResponseDto authTokenByRefreshToken(AuthTokenRequestDto authTokenRequestDto) {
        Optional<String> refreshToken = Optional.ofNullable(authTokenRequestDto.getRefreshToken());

        // Validate refresh token
        if(!jwtService.validateToken(refreshToken.orElse(""))) {
            throw ApiException.code("SCR0004");
        }

        Optional<LoginUser> loginUser = loginUserRepository.findByLoginId(authTokenRequestDto.getLoginId());

        if(!loginUser.map(LoginUser::getRefreshToken).equals(refreshToken)) {
            throw ApiException.code("SCR0005");
        }

        // Get Authentication
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        LoginUserDetails loginUserDetails = loginUserDetailsService.loadUserByUsername(authTokenRequestDto.getLoginId());
        if(!loginUserDetails.isAccountNonExpired()||!loginUserDetails.isAccountNonLocked()||!loginUserDetails.isEnabled()) {
            throw ApiException.code("SCR0006");
        }

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginUserDetails, null, loginUserDetails.getAuthorities());
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        return createJwtToken(authenticationToken);
    }

    private AuthTokenResponseDto createJwtToken(Authentication authentication) {
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();

        JwtTokenDto jwtTokenDto = jwtService.generateToken(authentication);

        AuthTokenResponseDto authTokenResponseDto = AuthTokenResponseDto.builder()
                .accessToken(jwtTokenDto.getAccessToken())
                .refreshToken(jwtTokenDto.getRefreshToken())
                .expiresIn(jwtTokenDto.getExpiresIn())
                .tokenType(jwtTokenDto.getTokenType())
                .build();

        loginUserRepository.updateRefreshTokenByUserId(loginUserDetails.getUserId(), jwtTokenDto.getRefreshToken());

        return authTokenResponseDto;
    }
}
