package com.home.quartzapp.security.service;

import com.home.quartzapp.security.dto.JwtTokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
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
        Map<String, Object> claims = new HashMap<>();
        LoginUserDetails userDetails = (LoginUserDetails) authentication.getPrincipal();
        claims.put("role", authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()));
        claims.put("displayName", userDetails.getDisplayName());
        return createToken(claims, authentication.getName());
    }

    public JwtTokenDto generateToken(String loginId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, loginId);
    }

    private JwtTokenDto createToken(Map<String, Object> claims, String loginId) {
        String accessToken;
        String refreshToken;

        long now = (new Date()).getTime();

        accessToken = Jwts.builder()
                .setSubject(loginId)
                .addClaims(claims)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expirationTimeSeconds * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
        refreshToken = Jwts.builder()
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

    public String extractUsername(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token)  throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
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

    public Boolean validateToken(String token, UserDetails userDetails) {
        if(this.validateToken(token)) {
            final String loginId = extractUsername(token);
            return loginId.equals(userDetails.getUsername());
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

    public UsernamePasswordAuthenticationToken getAuthentication(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, IllegalArgumentException {
        Claims claims = this.extractAllClaims(token);

        UserDetails userDetails = loginUserDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
