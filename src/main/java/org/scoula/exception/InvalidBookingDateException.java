package org.scoula.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 유효하지 않은 예약 날짜 요청 시 발생하는 예외
 */
@ResponseStatus(HttpStatus.BAD_REQUEST) // 이 예외 발생 시 400 상태 코드를 응답
public class InvalidBookingDateException extends RuntimeException {
	public InvalidBookingDateException(String message) {
		super(message);
	}
}
