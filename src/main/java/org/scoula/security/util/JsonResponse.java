package org.scoula.security.util;

import java.io.IOException;
import java.io.Writer;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * HTTP 응답을 JSON 형식으로 보내기 위한 유틸리티 클래스
 */
public class JsonResponse {
	/**
	 * 주어진 객체를 JSON 문자열로 변환하여 클라이언트에게 전송합니다.
	 * @param response HttpServletResponse 객체
	 * @param result 클라이언트에게 보낼 데이터 객체
	 * @param <T> 데이터 객체의 타입
	 */
	public static <T> void send(HttpServletResponse response, T result)
		throws IOException {
		ObjectMapper om = new ObjectMapper();

		response.setContentType("application/json;charset=UTF-8");
		Writer out = response.getWriter();
		out.write(om.writeValueAsString(result));
		out.flush();
	}

	/**
	 * 지정된 상태 코드와 에러 메시지를 클라이언트에게 전송합니다.
	 * @param response HttpServletResponse 객체
	 * @param status 설정할 HTTP 상태 코드
	 * @param message 전송할 에러 메시지
	 */
	public static void sendError(HttpServletResponse response, HttpStatus status, String message)
		throws IOException {
		response.setStatus(status.value());
		response.setContentType("application/json;charset=UTF-8");
		Writer out = response.getWriter();
		out.write(message);
		out.flush();
	}
}