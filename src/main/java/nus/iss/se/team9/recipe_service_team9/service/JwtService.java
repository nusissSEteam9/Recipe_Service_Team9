package nus.iss.se.team9.recipe_service_team9.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecret;

    // 解析 JWT 并返回用户名
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // 解析 JWT 并返回角色
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }
    // 解析 JWT 并返回 ID
    public Integer extractId(String token) {
        return extractClaim(token, claims -> claims.get("id", Integer.class));
    }
    // 通用方法，提取 JWT 中的指定声明（Claim）
    private <T> T extractClaim(String token, java.util.function.Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // 使用 JWT 密钥解析所有声明
    private Claims extractAllClaims(String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }
            return Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();
        } catch (SignatureException e) {
            throw new RuntimeException("Invalid JWT signature");
        }
    }
}
