package org.scoula.security.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.scoula.security.util.JsonResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

/**
 * 인증된 사용자가 필요한 권한 없이 보호된 리소스에 접근하려고 할 때 발생하는
 * 인가(Authorization) 실패를 처리하는 핸들러입니다. (HTTP 403 Forbidden)
 */
@Log4j2
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	/**
	 * 인가 실패 시 호출되어 클라이언트에게 403 Forbidden 에러를 JSON 형식으로 응답합니다.
	 */
	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
		AccessDeniedException accessDeniedException) throws IOException, ServletException {
		log.error("============= 인가 에러(Access Denied) =========");
		JsonResponse.sendError(response, HttpStatus.FORBIDDEN, "권한이 부족합니다.");
	}
}