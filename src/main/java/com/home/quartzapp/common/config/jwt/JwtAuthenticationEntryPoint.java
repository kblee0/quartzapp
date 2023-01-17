package com.home.quartzapp.common.config.jwt;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.home.quartzapp.common.util.HttpUtils;
import com.home.quartzapp.scheduler.dto.ApiErrorDto;
import com.home.quartzapp.scheduler.exception.ErrorCode;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        
        Exception e = (Exception)request.getAttribute("exception");

        if(e != null) {
            HttpUtils.sendApiError(response, new ApiErrorDto(ErrorCode.HTTP_UNAUTHORIZED, e.getMessage()));
        }
        else {
            HttpUtils.sendApiError(response, new ApiErrorDto(ErrorCode.HTTP_UNAUTHORIZED));
        }
    }
}
