package com.home.quartzapp.common.util;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.home.quartzapp.scheduler.dto.ApiErrorDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class HttpUtils {
    private static ObjectMapper objectMapper;

    public HttpUtils(ObjectMapper objectMapper) {
        HttpUtils.objectMapper = objectMapper;
    }
    public static void sendApiError(HttpServletResponse response, ApiErrorDto apiErrorDto) {
        PrintWriter writer = null;
        try {
            writer = response.getWriter();

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(apiErrorDto.getStatus().value());

            writer.write(objectMapper.writeValueAsString(apiErrorDto));
        } catch (IOException e) {
            log.error("JsonProcessingException.", e);
        }
        finally {
            if(writer != null) {
                writer.flush();
                writer.close();
            }
        }
    }
}
