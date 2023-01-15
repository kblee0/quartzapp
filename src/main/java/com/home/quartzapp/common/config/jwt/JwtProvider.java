package com.home.quartzapp.common.config.jwt;

import java.security.Key;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.home.quartzapp.users.dto.AccountDto;
import com.home.quartzapp.users.dto.UserDetailsDto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider implements InitializingBean {

    private final String secret;
    private final int tokenExpiresInSeconds;
    private Key key;

    public JwtProvider(
        @Value("${jwt.secret}") String secret,
        @Value("${jwt.token-expires-in-seconds}") int tokenExpiresInSeconds) {
        this.secret = secret;
        this.tokenExpiresInSeconds = tokenExpiresInSeconds;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(secret));
    }
    
    public String createToken(AccountDto accountDto) {
        String token;
        String roles;
        Date iat = new Date();

        roles = accountDto.getRoles().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(","));

        token = Jwts.builder()
            .setSubject(accountDto.getUsername()) // userId
            .claim("displayName", accountDto.getDisplayName())
            .claim("roles", roles)
            .setIssuedAt(iat)
            .setExpiration(new Date(iat.getTime() + tokenExpiresInSeconds*1000))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();

        return token;
    }

    public Authentication getAuthentication(String token) {
        Claims claims;
        AccountDto accountDto;
        List<SimpleGrantedAuthority> authorities;
        
        claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        
        authorities = Arrays.stream(claims.get("roles").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        accountDto = new AccountDto();

        accountDto.setAccountId("");
        accountDto.setUsername(claims.getSubject());
        accountDto.setPassword("N/A"); // password
        accountDto.setDisplayName(claims.getOrDefault("displayName", claims.getSubject()).toString());
        accountDto.setStatus("A");
        accountDto.setRoles(authorities);

        return new UsernamePasswordAuthenticationToken(new UserDetailsDto(accountDto), token, authorities);
    }

    public void validatonToken(String token) 
                throws JwtException, IllegalArgumentException {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
        } catch(Exception e) {
            throw e;
        }
    }
}
