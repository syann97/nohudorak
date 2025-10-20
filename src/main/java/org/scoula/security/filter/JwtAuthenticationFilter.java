package org.scoula.security.filter;

import java.io.IOException;
import java.util.Collections;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scoula.security.util.JwtProcessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * HTTP 요청 헤더에서 JWT를 감지하고, 유효한 경우 사용자를 인증하여
 * Spring Security의 SecurityContext에 인증 정보를 설정하는 필터입니다.
 * 모든 요청에 대해 한 번씩 실행됩니다.
 */
@Log4j2
@RequiredArgsConstructor
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	/** JWT가 담길 HTTP 요청 헤더의 이름 */
	private static final String AUTHORIZATION_HEADER = "Authorization";

	/** JWT 토큰의 표준 접두사 */
	public static final String BEARER_PREFIX = "Bearer "; // 끝에 공백 포함

	/** JWT 토큰의 유효성 검사 및 정보 추출을 담당하는 컴포넌트 */
	private final JwtProcessor jwtProcessor;

	/**
	 * 유효한 JWT 토큰으로부터 Authentication 객체를 생성합니다.
	 * @param token 유효성이 검증된 JWT 문자열
	 * @return 생성된 Authentication 객체
	 */
	private Authentication getAuthentication(String token) {
		// JWT에서 사용자 식별자(email)를 추출합니다.
		String email = jwtProcessor.getUsername(token);

		// UsernamePasswordAuthenticationToken을 사용하여 인증 객체를 생성합니다.
		// 여기서는 별도의 권한(Role)을 설정하지 않으므로 비어있는 리스트를 전달합니다.
		return new UsernamePasswordAuthenticationToken(email, null, Collections.emptyList());
	}

	/**
	 * 모든 요청을 가로채 JWT 인증을 처리하는 핵심 메소드입니다.
	 * @param request  들어오는 HTTP 요청
	 * @param response 나가는 HTTP 응답
	 * @param filterChain 다음 필터로 요청을 전달하기 위한 필터 체인
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {

		log.debug("JwtAuthenticationFilter running for URI: {}", request.getRequestURI());

		// 1. 요청 헤더에서 'Authorization' 헤더를 통해 토큰을 추출합니다.
		String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

		// 2. 토큰이 존재하고 'Bearer '로 시작하는지 확인합니다.
		if (bearerToken != null && bearerToken.startsWith(BEARER_PREFIX)) {
			// 'Bearer ' 접두사를 제거하여 순수한 토큰 문자열만 추출합니다.
			String token = bearerToken.substring(BEARER_PREFIX.length());

			// 2-1. 토큰의 유효성을 검증합니다.
			if (jwtProcessor.validateToken(token)) {
				// 2-2. 토큰이 유효하면, Authentication 객체를 생성하여 SecurityContextHolder에 저장합니다.
				// 이로써 해당 요청은 인증된 것으로 간주됩니다.
				Authentication authentication = getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(authentication);
				log.debug("Authentication successful for user: {}", authentication.getName());
			} else {
				log.warn("Invalid JWT token received.");
			}
		} else {
			log.debug("Authorization header is missing or does not start with Bearer.");
		}

		// 3. 다음 필터로 요청과 응답을 전달합니다.
		// 토큰이 없거나 유효하지 않더라도 필터 체인은 계속 진행되어야 합니다.
		// 최종적인 접근 허용 여부는 SecurityConfig의 authorizeRequests 설정에 따라 결정됩니다.
		filterChain.doFilter(request, response);
	}
}