package com.home.quartzapp.common.config.jwt;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.home.quartzapp.common.util.HttpUtils;
import com.home.quartzapp.scheduler.dto.ApiErrorDto;
import com.home.quartzapp.scheduler.exception.ErrorCode;

@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        HttpUtils.sendApiError(response, new ApiErrorDto(ErrorCode.HTTP_FORBIDDEN));
    }
    
}
