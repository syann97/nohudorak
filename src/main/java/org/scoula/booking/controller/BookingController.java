package org.scoula.booking.controller;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.util.List;

import org.scoula.booking.dto.BookingCheckResponseDto;
import org.scoula.booking.dto.BookingCreateRequestDto;
import org.scoula.booking.dto.BookingCreateResponseDto;
import org.scoula.booking.dto.BookingDetailResponseDto;
import org.scoula.booking.dto.BookingDto;
import org.scoula.booking.dto.BookingPatchRequestDto;
import org.scoula.booking.dto.ReservedSlotsResponseDto;
import org.scoula.booking.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "예약 API", description = "예약 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/bookings")
public class BookingController {

	private final BookingService bookingService;

	@ApiOperation(value = "신규 예약 생성", notes = "새로운 상담 예약을 생성합니다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "예약 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청 데이터 (유효하지 않은 날짜 등)"),
		@ApiResponse(code = 409, message = "이미 예약된 시간이거나 중복된 예약")
	})
	@PostMapping
	public ResponseEntity<BookingCreateResponseDto> addBooking(
		Authentication authentication,
		@RequestBody BookingCreateRequestDto requestDto) {

		String email = authentication.getName();
		BookingCreateResponseDto responseDto = bookingService.addBooking(email, requestDto);

		URI location = ServletUriComponentsBuilder
			.fromCurrentRequest()
			.path("/{id}")
			.buildAndExpand(responseDto.getBookingCode())
			.toUri();

		bookingService.sendBookingToBank(email, requestDto, responseDto);

		return ResponseEntity.created(location).body(responseDto);
	}

	@ApiOperation(value = "내 예약 목록 조회", notes = "현재 로그인한 사용자의 모든 예약 목록을 조회합니다.")
	@ApiResponse(code = 200, message = "조회 성공")
	@GetMapping("/user")
	public ResponseEntity<List<BookingDto>> getMyBookings(Authentication authentication) {
		String email = authentication.getName();
		List<BookingDto> bookings = bookingService.getBookingsByEmail(email);
		return ResponseEntity.ok(bookings);
	}

	@ApiOperation(value = "예약 상세 조회", notes = "예약 ID로 특정 예약의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 예약")
	})
	@GetMapping("/detail/{identifier}")
	public ResponseEntity<BookingDetailResponseDto> getBookingDetail(
		@ApiParam(value = "조회할 예약의 식별자 (ULID 또는 예약 코드)", required = true, example = "250810-B01-001")
		@PathVariable String identifier, Authentication authentication) {
		String email = authentication.getName();
		BookingDetailResponseDto responseDto = bookingService.getBookingDetailByIdentifier(identifier, email);
		return ResponseEntity.ok(responseDto);
	}

	@ApiOperation(value = "예약 정보 부분 수정", notes = "예약의 날짜 또는 시간을 수정합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "수정 성공"),
		@ApiResponse(code = 403, message = "수정 권한 없음"),
		@ApiResponse(code = 404, message = "존재하지 않는 예약"),
		@ApiResponse(code = 409, message = "변경하려는 시간에 이미 예약이 존재함")
	})
	@PatchMapping("/{bookingId}")
	public ResponseEntity<BookingDetailResponseDto> patchBooking(
		@ApiParam(value = "수정할 예약의 ID(ULID)", required = true, example = "01H8XJ6B4T1Z0V0E0M0R0P0W0")
		@PathVariable String bookingId,
		@RequestBody BookingPatchRequestDto patchDto,
		Authentication authentication) throws AccessDeniedException {

		String email = authentication.getName();
		BookingDetailResponseDto updatedBooking = bookingService.patchBooking(bookingId, email, patchDto);
		return ResponseEntity.ok(updatedBooking);
	}

	@ApiOperation(value = "예약 삭제", notes = "예약 ID로 특정 예약을 삭제합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "삭제 성공"),
		@ApiResponse(code = 403, message = "삭제 권한 없음"),
		@ApiResponse(code = 404, message = "존재하지 않는 예약")
	})
	@DeleteMapping("/{bookingId}")
	public ResponseEntity<Void> deleteBooking(
		@ApiParam(value = "삭제할 예약의 ID(ULID)", required = true, example = "01H8XJ6B4T1Z0V0E0M0R0P0W0")
		@PathVariable String bookingId,
		Authentication authentication) throws AccessDeniedException {

		String currentUserEmail = authentication.getName();
		bookingService.deleteBooking(bookingId, currentUserEmail);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "특정 상품에 대한 예약 존재 여부 확인", notes = "현재 사용자가 특정 금융 상품을 이미 예약했는지 확인합니다.")
	@ApiResponse(code = 200, message = "조회 성공 (예약이 있으면 isBooked=true, 없으면 isBooked=false)")
	@GetMapping("/check/{finPrdtCode}")
	public ResponseEntity<BookingCheckResponseDto> checkBookingExists(
		@ApiParam(value = "확인할 금융 상품의 코드", required = true, example = "LN200300000002")
		@PathVariable String finPrdtCode,
		Authentication authentication) {

		String email = authentication.getName();
		BookingCheckResponseDto responseDto = bookingService.checkBookingExists(email, finPrdtCode);
		return ResponseEntity.ok(responseDto);
	}

	@ApiOperation(value = "특정 지점의 예약된 시간 목록 조회", notes = "특정 지점의 예약된 날짜와 시간 목록을 조회합니다.")
	@ApiResponse(code = 200, message = "조회 성공")
	@GetMapping("/{branchId}/reserved-slots")
	public ResponseEntity<ReservedSlotsResponseDto> getReservedSlots(
		@ApiParam(value = "조회할 지점의 ID", required = true, example = "1")
		@PathVariable Integer branchId) {
		ReservedSlotsResponseDto responseDto = bookingService.getReservedSlotsByBranch(branchId);
		return ResponseEntity.ok(responseDto);
	}
}
