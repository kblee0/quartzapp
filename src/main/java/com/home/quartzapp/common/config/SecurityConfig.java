package com.home.quartzapp.common.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.home.quartzapp.common.config.jwt.JwtAccessDeniedHandler;
import com.home.quartzapp.common.config.jwt.JwtAuthenticationEntryPoint;
import com.home.quartzapp.common.config.jwt.JwtProvider;
import com.home.quartzapp.common.config.jwt.JwtSecurityConfigurerAdapter;

import lombok.RequiredArgsConstructor;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring()
            .antMatchers("/favicon.ico")
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }
    
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        
        httpSecurity
            .csrf().disable()

            // 401, 403 exception handling
            .exceptionHandling()
            .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .accessDeniedHandler(jwtAccessDeniedHandler)

            // not use session
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

            // login api
            .and()
            .authorizeRequests()
            .antMatchers("/v1/users/login").permitAll();

        // AuthorizationManager
        httpSecurity
            .authorizeRequests()
            .antMatchers("/scheduler/**").hasRole("ADMIN"); // ROLE_ADMIN

        httpSecurity
            .authorizeRequests()
            .anyRequest()
            .authenticated()

            .and()
            .apply(new JwtSecurityConfigurerAdapter(jwtProvider));

        return httpSecurity.build();
    }
}
