package com.home.quartzapp.common.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtProvider jwtProvider;
//    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
//    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
                // remove at spring boot 3
                // .antMatchers("/favicon.ico")
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {

        // JWT Spring Boot 3 오류로 임시 삭제
        //        httpSecurity
        //            .csrf(AbstractHttpConfigurer::disable)
        //            .authenticationProvider(jwtAuthenticationEntryPoint)
        //            .accessDeniedHandler(jwtAccessDeniedHandler);

        // AuthorizationManager
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/favicon.ico").permitAll()
                        .requestMatchers("/v1/users/login").permitAll()     // login api
                        .requestMatchers("/join").permitAll()
                        .requestMatchers("/h2-console").permitAll()
                        .requestMatchers("/scheduler/**").hasRole("ADMIN")  // ROLE_ADMIN
                        .anyRequest().authenticated())
                .sessionManagement(manager -> manager.sessionCreationPolicy(SessionCreationPolicy.STATELESS))     // not use session
                .exceptionHandling(configurer -> {
                    configurer.authenticationEntryPoint(((request, response, authException) -> {

                    }));
                    configurer.accessDeniedHandler(((request, response, accessDeniedException) -> {

                    }));
                })
        ;
        // JWT Spring Boot 3 오류로 임시 삭제
        //        httpSecurity
        //            .authorizeRequests()
        //            .anyRequest()
        //            .authenticated()
        //
        //            .and()
        //            .apply(new JwtSecurityConfigurerAdapter(jwtProvider));

        return httpSecurity.build();
    }
}
