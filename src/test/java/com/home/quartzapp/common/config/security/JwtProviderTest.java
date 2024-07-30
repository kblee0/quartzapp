package com.home.quartzapp.common.config.security;

import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = {
    "jwt.secret-key=aG91Mjctc2ltcGxlLXNwcmluZy1ib290LWFwaS1qd3QK",
    "exprirs-in-msec=60000"
})
public class JwtProviderTest {
    @Test
    void createToken() {
        /*
        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        AccountDto accountDto = new AccountDto();
        accountDto.setUsername("testuser");
        accountDto.setPassword("testPassword");
        accountDto.setDisplayName("test display name");
        List<SimpleGrantedAuthority> roles = new ArrayList<SimpleGrantedAuthority>();

        roles.add(new SimpleGrantedAuthority("ROLE_USER"));
        roles.add(new SimpleGrantedAuthority("ROLE_ADMIN"));

        UsernamePasswordAuthenticationToken authenticationToken;
        authenticationToken = new UsernamePasswordAuthenticationToken(accountDto.getUsername(), accountDto.getPassword());

        AuthenticationManagerBuilder authenticationManagerBuilder = new AuthenticationManagerBuilder();
        Authentication authentication;

        authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        JwtProvider jwtProvider = new JwtProvider("aG91Mjctc2ltcGxlLXNwcmluZy1ib290LWFwaS1qd3QK", 60);
        jwtProvider.afterPropertiesSet();
        jwtProvider.createToken(accountDto, authentication);
    */
    }
}
