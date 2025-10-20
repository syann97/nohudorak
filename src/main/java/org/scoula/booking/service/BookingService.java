package org.scoula.booking.service;

import java.nio.file.AccessDeniedException;
import java.util.List;

import org.scoula.booking.dto.BookingCheckResponseDto;
import org.scoula.booking.dto.BookingCreateRequestDto;
import org.scoula.booking.dto.BookingCreateResponseDto;
import org.scoula.booking.dto.BookingDetailResponseDto;
import org.scoula.booking.dto.BookingDto;
import org.scoula.booking.dto.BookingPatchRequestDto;
import org.scoula.booking.dto.ReservedSlotsResponseDto;

/**
 * 예약 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface BookingService {
	/**
	 * 특정 사용자의 모든 예약 목록을 조회합니다.
	 * @param email 사용자 이메일
	 * @return 해당 사용자의 예약 목록 DTO 리스트
	 */
	List<BookingDto> getBookingsByEmail(String email);

	/**
	 * 새로운 예약을 생성합니다.
	 * @param email 예약을 생성하는 사용자 이메일
	 * @param requestDto 예약 생성에 필요한 정보가 담긴 DTO
	 * @return 생성된 예약의 정보가 담긴 응답 DTO
	 */
	BookingCreateResponseDto addBooking(String email, BookingCreateRequestDto requestDto);

	/**
	 * 은행 서버로 예약 정보를 전달합니다.
	 * @param email
	 * @param requestDto
	 * @param responseDto
	 */
	void sendBookingToBank(String email, BookingCreateRequestDto requestDto,
		BookingCreateResponseDto responseDto); // 은행 서버 전송

	/**
	 * 특정 예약을 삭제합니다.
	 * @param bookingId 삭제할 예약의 ID
	 * @param currentUserEmail 삭제를 요청한 사용자 이메일 (권한 확인용)
	 * @throws AccessDeniedException 삭제 권한이 없을 경우 발생
	 */
	void deleteBooking(String bookingId, String currentUserEmail) throws AccessDeniedException;

	/**
	 * 예약 ID로 특정 예약의 상세 정보를 조회합니다.
	 * @param bookingId 조회할 예약의 ID
	 * @return 예약 상세 정보 DTO
	 */
	BookingDetailResponseDto getBookingById(String bookingId);

	BookingDetailResponseDto getBookingDetailByIdentifier(String identifier, String email);

	/**
	 * 특정 지점의 예약된 시간 슬롯 목록을 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 예약된 날짜와 시간 정보가 담긴 DTO
	 */
	ReservedSlotsResponseDto getReservedSlotsByBranch(int branchId);

	/**
	 * 특정 사용자가 해당 금융 상품을 이미 예약했는지 확인합니다.
	 * @param email 사용자 이메일
	 * @param prdtCode 확인할 금융 상품 코드
	 * @return 예약 존재 여부 및 정보가 담긴 DTO
	 */
	BookingCheckResponseDto checkBookingExists(String email, String prdtCode);

	/**
	 * 예약 정보를 부분 수정합니다 (날짜, 시간 등).
	 * @param bookingId 수정할 예약의 ID
	 * @param currentUserEmail 수정을 요청한 사용자의 이메일 (권한 확인용)
	 * @param patchDto 수정할 정보가 담긴 DTO
	 * @return 수정된 예약의 상세 정보 DTO
	 * @throws AccessDeniedException 수정 권한이 없을 경우 발생
	 */
	BookingDetailResponseDto patchBooking(String bookingId, String currentUserEmail,
		BookingPatchRequestDto patchDto) throws
		AccessDeniedException;

}
