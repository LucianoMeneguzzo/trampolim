package com.trampolim.api.service;

import com.trampolim.api.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private SecretKey signInKey;

    // Monta a chave uma vez, quando o backend sobe.
    // Sem JWT_SECRET (sem o backend/.env), sorteia uma chave: dá para testar, mas os logins caem ao reiniciar.
    @PostConstruct
    void carregarChave() {
        if (secretKey == null || secretKey.isBlank()) {
            log.warn("JWT_SECRET não definido: usando uma chave aleatória. Os tokens deixam de valer quando o backend reiniciar.");
            signInKey = Jwts.SIG.HS256.key().build();
        } else {
            signInKey = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Payload do token: sub = e-mail, userId, type (STUDENT/MENTOR), iat e exp.
    // O front lê "exp" para saber quando o login expirou.
    public String generateToken(User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("type", user.getType().name())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSignInKey())
                .compact();
    }

    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equalsIgnoreCase(username) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        return signInKey;
    }
}
