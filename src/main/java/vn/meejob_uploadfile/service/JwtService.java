package vn.meejob_uploadfile.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.meejob_uploadfile.model.Account;
import vn.meejob_uploadfile.service.JwtService;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtService {

    @Value("${jwt.accessToken.secretKey}")
    private String accessKey;

    @Value("${jwt.accessToken.expiry}")
    private long accessTokenExpiry;

    private Key getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessKey));
    }

    public Map<String, Object> buildClaims(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        if (user instanceof Account account) {
            claims.put("role", account.getRole());
            claims.put("userId", account.getId());
        }
        return claims;
    }

    public String generateAccessToken(UserDetails user) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(buildClaims(user))
                .setSubject(user.getUsername())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + accessTokenExpiry))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public boolean isTokenExpired(String token) {
        return extractClaim(token, claims -> claims.getExpiration().before(new Date()));
    }

    public boolean validateToken(String token, UserDetails user) {
        String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

}
