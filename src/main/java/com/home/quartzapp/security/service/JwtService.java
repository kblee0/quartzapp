package com.home.quartzapp.security.service;

import com.home.quartzapp.security.dto.JwtTokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Getter
@Slf4j
public class JwtService {
    private final String secretKey;
    private final long expirationTimeSeconds;
    private final long refreshExpirationTimeSeconds;
    private final LoginUserDetailsService loginUserDetailsService;

    public JwtService(
            @Value("${jwt.secret}")String secretKey,
            @Value("${jwt.expiration-time-seconds}")long expirationTimeSeconds,
            @Value("${jwt.refresh-expiration-time-seconds}")long refreshExpirationTimeSeconds,
            LoginUserDetailsService loginUserDetailsService) {
        this.secretKey = secretKey;
        this.expirationTimeSeconds = expirationTimeSeconds;
        this.refreshExpirationTimeSeconds = refreshExpirationTimeSeconds;
        this.loginUserDetailsService = loginUserDetailsService;
    }

    public JwtTokenDto generateToken(Authentication authentication) {
        LoginUserDetails loginUserDetails = (LoginUserDetails) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("loginId", loginUserDetails.getLoginId());
        claims.put("displayName", loginUserDetails.getDisplayName());
        claims.put("role", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));

        long now = (new Date()).getTime();

        String accessToken = Jwts.builder()
                .setSubject(loginUserDetails.getUserId())
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTimeSeconds * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
        String refreshToken = Jwts.builder()
                .setExpiration(new Date(now + refreshExpirationTimeSeconds * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();

        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(expirationTimeSeconds)
                .tokenType("Bearer").build();
    }

    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        Jws<Claims> jwsClaims = Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token);

        return jwsClaims.getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, LoginUserDetails loginUserDetails) {
        if(this.validateToken(token)) {
            final String userId = extractUserId(token);
            return userId.equals(loginUserDetails.getUserId());
        }
        return false;
    }
    public Boolean validateToken(String token) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            log.debug("Invalid JWT Token. {}", e.getMessage());
        } catch(ExpiredJwtException e) {
            log.debug("Expired JWT Token. {}", e.getMessage());
        } catch(UnsupportedJwtException e) {
            log.debug("Unsupported JWT Token. {}", e.getMessage());
        }
        catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty. {}", e.getMessage());
        }
        return false;
    }
    public Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
        Claims claims = this.extractAllClaims(token);

        // LoginUserDetails loginUserDetails = loginUserDetailsService.loadUserByUsername(claims.getSubject());

        List<GrantedAuthority> authorities = ((List<String>)claims.get("role")).stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        LoginUserDetails loginUserDetails = new LoginUserDetails();

        loginUserDetails.setUserId(claims.getSubject());
        loginUserDetails.setLoginId((String)claims.get("loginId"));
        loginUserDetails.setDisplayName((String)claims.get("displayName"));
        loginUserDetails.setAuthorities(authorities);

        return new UsernamePasswordAuthenticationToken(loginUserDetails, null, loginUserDetails.getAuthorities());
    }
}
