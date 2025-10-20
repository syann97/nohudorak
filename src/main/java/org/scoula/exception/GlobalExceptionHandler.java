package org.scoula.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import lombok.extern.log4j.Log4j2;

@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {
	/**
	 * 외부 API 요청 실패 시 처리 (예: 카카오 API 등)
	 * @param error HttpClientErrorException 객체
	 * @return 외부 요청 오류에 대한 적절한 에러 응답
	 */
	@ExceptionHandler(HttpClientErrorException.class)
	public ResponseEntity<ErrorResponse> handleHttpClientError(HttpClientErrorException error) {
		log.error("Http client Error: {}", error.getMessage());
		if (error.getStatusCode() == HttpStatus.BAD_REQUEST && error.getResponseBodyAsString().contains("KOE320")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
				.body(new ErrorResponse("유효하지 않은 코드입니다.", error.getResponseBodyAsString()));
		}
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("외부 서버 통신 장애", error.getResponseBodyAsString()));
	}

	/**
	 * 사용자가 권한이 없는 리소스에 접근할 때 발생
	 * @param ex UserAccessDeniedException 객체
	 * @return 403 Forbidden 에러 응답
	 */
	@ExceptionHandler(UserAccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleCustomAccessDenied(UserAccessDeniedException ex) {
		log.warn("Access denied: {}", ex.getMessage());
		ErrorResponse response = new ErrorResponse("FORBIDDEN", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
	}

	/**
	 * 잘못된 예약 날짜를 요청했을 때 발생
	 * @param ex InvalidBookingDateException 객체
	 * @return 400 Bad Request 에러 응답
	 */
	@ExceptionHandler(InvalidBookingDateException.class)
	public ResponseEntity<ErrorResponse> handleInvalidBookingDate(InvalidBookingDateException ex) {
		ErrorResponse response = new ErrorResponse("INVALID_DATE", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}

	/**
	 * 요청하는 상품이 없거나 상품 테이블이 비었을 때 요청을 처리하는 핸들러
	 */
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException ex) {
		ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	/**
	 * UserNotFoundException 발생 시 처리하는 핸들러
	 */
	@ExceptionHandler(UserNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleUserNotFound(ProductNotFoundException ex) {
		ErrorResponse error = new ErrorResponse("NOT_FOUND", ex.getMessage());
		return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
	}

	/**
	 * 존재하지 않는 지점 ID로 요청 시 처리하는 핸들러
	 * @param ex 예외 객체
	 */
	@ExceptionHandler(BranchNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleBranchNotFound(BranchNotFoundException ex) {
		log.warn("Branch not found: {}", ex.getMessage());
		ErrorResponse response = new ErrorResponse("BRANCH_NOT_FOUND", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND); // 404 Not Found
	}

	/**
	 * 잘못된 파라미터 타입
	 * @param error 에러 객체
	 * */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException error) {
		log.error("파라미터 타입 불일치: {}", error.getMessage());
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("잘못된 요청 파라미터", error.getMessage()));
	}

	//NPE, 일반 예외

	/**
	 * NullPointException
	 * 일반 예외
	 * @param error 에러 객체
	 * */
	@ExceptionHandler(NullPointerException.class)
	public ResponseEntity<ErrorResponse> handleNpe(NullPointerException error) {
		log.error("NullPointerException: {}", error.getMessage(), error);

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("서버 내부 오류", "필수 정보가 누락됐거나 잘못되었습니다."));
	}

	/**
	 * 400 Bad Request
	 * 잘못된 파라미터로 요청하는 경우
	 * @param error 에러 객체
	 * */
	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException error) {
		return ResponseEntity.badRequest()
			.body(new ErrorResponse("잘못된 요청", error.getMessage()));
	}

	/**
	 * 특정 지점에 선택한 시간에 이미 예약이 존재하는 경우에 대한 에러
	 * @param ex 예외 객체
	 * */
	@ExceptionHandler(DuplicateBookingException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateBooking(DuplicateBookingException ex) {
		// 클라이언트에게 보낼 에러 응답 DTO 생성
		ErrorResponse response = new ErrorResponse("DUPLICATE_BOOKING", ex.getMessage());

		// 409 Conflict 상태 코드와 함께 응답 반환
		return new ResponseEntity<>(response, HttpStatus.CONFLICT);
	}

	/**
	 * 나머지 일반적인 예외 통합 처리
	 * @param error 에러 객체
	 * */
	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception error) {
		log.error("X 알 수 없는 예외 발생: {}", error.getMessage(), error);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("서버 내부 오류", error.getMessage()));
	}

	/**
	 * HTTP 요청 바디가 올바른 JSON 형식이 아닌 경우 발생
	 * @param error 예외 객체
	 * @return 잘못된 JSON 포맷에 대한 에러 응답
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleInvalidFormat(HttpMessageNotReadableException error) {
		log.error("잘못된 Json Format", error.getMessage(), error);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(new ErrorResponse("잘못된 JSON 포맷", error.getMessage()));
	}

	/**
	 * 요청한 자산이 존재하지 않을 때 발생
	 * @param ex AssetNotFoundException 객체
	 * @return 404 Not Found 에러 응답
	 */
	@ExceptionHandler(AssetNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleAssetNotFound(AssetNotFoundException ex) {
		log.warn("Asset not found: {}", ex.getMessage());
		ErrorResponse response = new ErrorResponse("ASSET_NOT_FOUND", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}

	/**
	 * 요청한 예약이 존재하지 않을 때 발생
	 * @param ex BookingNotFoundException 객체
	 * @return 404 Not Found 에러 응답
	 */
	@ExceptionHandler(BookingNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleBookingNotFound(BookingNotFoundException ex) {
		log.warn("Booking not found: {}", ex.getMessage());
		ErrorResponse response = new ErrorResponse("BOOKING_NOT_FOUND", ex.getMessage());
		return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
	}
}

