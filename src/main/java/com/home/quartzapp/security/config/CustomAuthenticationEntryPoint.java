package com.home.quartzapp.security.config;

import com.home.quartzapp.common.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) {
        log.error("Request URI: {}", request.getRequestURI());

        ApiException.code("CMNE0005", authException).log().responseWrite(response);
    }
}
