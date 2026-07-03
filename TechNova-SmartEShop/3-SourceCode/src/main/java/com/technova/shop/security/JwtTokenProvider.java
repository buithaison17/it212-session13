package com.technova.shop.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Lớp tiện ích cung cấp các cơ chế sinh mã, giải mã và validate JWT token.
 * Sử dụng thư viện JJWT version 0.12.x.
 */
@Component
@Slf4j
public class JwtTokenProvider {

    private final SecretKey jwtSecretKey;
    private final long jwtExpirationInMs;

    public JwtTokenProvider(
            @Value("${technova.jwt.secret}") String secret,
            @Value("${technova.jwt.expiration}") long expiration) {
        // Giải mã khóa bí mật đã được encode Base64 và tạo SecretKey phù hợp với thuật toán HS256
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.jwtSecretKey = Keys.hmacShaKeyFor(keyBytes);
        this.jwtExpirationInMs = expiration;
    }

    /**
     * Tạo JWT token sau khi đăng nhập thành công.
     */
    public String generateToken(Authentication authentication) {
        UserDetailsImpl userPrincipal = (UserDetailsImpl) authentication.getPrincipal();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInMs);

        return Jwts.builder()
                .subject(userPrincipal.getUsername())
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(jwtSecretKey)
                .compact();
    }

    /**
     * Lấy username từ JWT token.
     */
    public String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(jwtSecretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    /**
     * Kiểm tra tính hợp lệ của JWT token.
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(jwtSecretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (SignatureException ex) {
            log.error("Chữ ký JWT không hợp lệ");
        } catch (MalformedJwtException ex) {
            log.error("Token JWT không đúng định dạng");
        } catch (ExpiredJwtException ex) {
            log.error("Token JWT đã hết hạn");
        } catch (UnsupportedJwtException ex) {
            log.error("Token JWT không được hỗ trợ");
        } catch (IllegalArgumentException ex) {
            log.error("Chuỗi JWT claims rỗng hoặc không hợp lệ");
        }
        return false;
    }
}
