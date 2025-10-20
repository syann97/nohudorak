package org.scoula.exception;

/**
 * 접근 권한이 없을 때 발생하는 커스텀 예외
 */
public class UserAccessDeniedException extends RuntimeException {
	public UserAccessDeniedException(String message) {
		super(message);
	}
}
