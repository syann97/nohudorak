package org.scoula.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 이 예외가 발생하면 409 Conflict 상태 코드를 응답
public class DuplicateBookingException extends RuntimeException {
	public DuplicateBookingException(String message) {
		super(message);
	}
}
