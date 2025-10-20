package org.scoula.security.handler;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근하려고 할 때 발생하는
 * 인증(Authentication) 실패를 처리하는 진입점입니다. (HTTP 401 Unauthorized)
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
	private final ObjectMapper objectMapper = new ObjectMapper();

	/**
	 * 인증 실패 시 호출되어 클라이언트에게 401 Unauthorized 에러를 JSON 형식으로 응답합니다.
	 */
	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
		AuthenticationException authException) throws IOException {

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 상태 코드 설정
		response.setContentType("application/json;charset=UTF-8");

		// 에러 메시지를 포함한 JSON 응답 생성
		Map<String, String> errorResponse = Map.of(
			"detail", authException.getMessage(),
			"error", "인증이 필요합니다."
		);

		response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
	}
}