package com.home.quartzapp.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI openAPI() {
        final String JWT_COMPONENT_NAME = "JWT Token";
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(JWT_COMPONENT_NAME);
        Components components = new Components().addSecuritySchemes(
                JWT_COMPONENT_NAME,
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("Bearer").bearerFormat("JWT"));

        return new OpenAPI()
                .components(new Components())
                .info(apiInfo())
                .addSecurityItem(securityRequirement)
                .components(components);
    }

    private Info apiInfo() {
        return new Info()
                .title("Quartz app API Test")
                .description("Quartz app API Test")
                .version("1.0.0");
    }
}
