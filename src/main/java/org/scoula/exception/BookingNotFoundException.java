package org.scoula.exception;

/**
 * 예약을 찾을 수 없을 때 발생하는 커스텀 예외
 */
public class BookingNotFoundException extends RuntimeException {
	public BookingNotFoundException(String message) {
		super(message);
	}
}
