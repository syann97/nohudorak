package org.scoula.security.util;

import io.jsonwebtoken.SignatureAlgorithm;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import java.util.Map;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

/**
 * JWT(Json Web Token)의 생성, 검증, 정보 추출을 담당하는 유틸리티 클래스
 */
@Component
public class JwtProcessor {

  // 액세스 토큰 유효 기간: 1시간
  private static final long ACCESS_TOKEN_VALID_MILISECOND = 1000L * 60 * 60;
  // 리프레시 토큰 유효 기간: 2주
  private static final long REFRESH_TOKEN_VALID_MILISECOND = 1000L * 60 * 60 * 24 * 14;

  // JWT 서명에 사용할 비밀키
  private final String secretKey = "비밀키는 충반한 길이의 문자열이어야 한다";
  private final Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

  /**
   * 액세스 토큰을 생성합니다.
   *
   * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
   * @return 생성된 JWT 문자열
   */
  public String generateAccessToken(String subject) {
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(new Date().getTime() + ACCESS_TOKEN_VALID_MILISECOND)) // 1시간
        .signWith(key)
        .compact();
  }

  /**
   * 리프레시 토큰을 생성합니다.
   *
   * @param subject 토큰의 주체 (일반적으로 사용자 이메일)
   * @return 생성된 JWT 문자열
   */
  public String generateRefreshToken(String subject) {
    return Jwts.builder()
        .setSubject(subject)
        .setIssuedAt(new Date())
        .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_VALID_MILISECOND)) // 2주
        .signWith(key)
        .compact();
  }

  /**
   * JWT의 유효성을 검증합니다. (서명, 유효기간 등)
   *
   * @param token 검증할 JWT 문자열
   * @return 유효하면 true, 아니면 예외 발생
   */
  public boolean validateToken(String token) {
    Jws<Claims> claims = Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token);
    return true;
  }

  /**
   * 클레임을 함께 담은 액세스 토큰 생성
   */
  public String generateAccessToken(String subject, Map<String, Object> claims) {
    return Jwts.builder()
        .setSubject(subject)
        .addClaims(claims) // name, email 등 커스텀 클레임 주입
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_VALID_MILISECOND))
        .signWith(key)
        .compact();
  }

  /**
   * JWT에서 사용자 이메일(Subject)을 추출합니다. 토큰 해석이 불가능한 경우(서명 오류, 만료 등) 예외가 발생합니다.
   *
   * @param token Access Token 문자열
   * @return 사용자 이메일
   */
  public String getUsername(String token) {
    return Jwts.parserBuilder()
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}