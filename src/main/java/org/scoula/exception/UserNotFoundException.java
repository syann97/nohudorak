package org.scoula.exception;

// @ResponseStatus 어노테이션은 GlobalExceptionHandler가 있을 경우, 핸들러가 우선권을 가집니다.
// 핸들러가 없을 때를 대비한 기본 방어 장치로 유용합니다.

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND) // 기본적으로 404 Not Found를 반환하도록 설정
public class UserNotFoundException extends RuntimeException {

	// 예외 메시지를 받는 생성자
	public UserNotFoundException(String message) {
		super("상품을 찾을 수 없습니다: " + message);
	}
}
