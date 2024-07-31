package com.home.quartzapp.security.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtService {
    private final String secretKey;
    private final long expirationTimeSeconds;

    public JwtService(@Value("${jwt.secret}")String secretKey, @Value("${jwt.expiration-time-seconds}")long expirationTimeSeconds) {
        this.secretKey = secretKey;
        this.expirationTimeSeconds = expirationTimeSeconds;
    }

    public String generateToken(String loginId) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, loginId);
    }

    private String createToken(Map<String, Object> claims, String loginId) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(loginId)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTimeSeconds * 1000))
                .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private Key getSignKey() {
        byte[] keyBytes= Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) throws ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException, IllegalArgumentException {
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

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(getSignKey())
                    .build()
                    .parseClaimsJws(token);

            final String loginId = claims.getBody().getSubject();
            return loginId.equals(userDetails.getUsername());
        } catch (io.jsonwebtoken.security.SignatureException | MalformedJwtException e) {
            log.debug("Invalid JWT Token", e.getMessage());
        } catch(ExpiredJwtException e) {
            log.debug("Expired JWT Token", e.getMessage());
        } catch(UnsupportedJwtException e) {
            log.debug("Unsupported JWT Token", e.getMessage());
        }
        catch (IllegalArgumentException e) {
            log.debug("JWT claims string is empty", e.getMessage());
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
}
