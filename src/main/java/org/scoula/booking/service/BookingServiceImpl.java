package org.scoula.booking.service;

import java.nio.file.AccessDeniedException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scoula.booking.domain.BookingVo;
import org.scoula.booking.dto.BankBookingRequestDto;
import org.scoula.booking.dto.BookingCheckDetailDto;
import org.scoula.booking.dto.BookingCheckResponseDto;
import org.scoula.booking.dto.BookingCreateRequestDto;
import org.scoula.booking.dto.BookingCreateResponseDto;
import org.scoula.booking.dto.BookingDetailResponseDto;
import org.scoula.booking.dto.BookingDto;
import org.scoula.booking.dto.BookingPatchRequestDto;
import org.scoula.booking.dto.DocInfoDto;
import org.scoula.booking.dto.ReservedSlotsResponseDto;
import org.scoula.booking.mapper.BookingMapper;
import org.scoula.branch.service.BranchService;
import org.scoula.exception.DuplicateBookingException;
import org.scoula.exception.InvalidBookingDateException;
import org.scoula.exception.UserAccessDeniedException;
import org.scoula.product.service.ProductService;
import org.scoula.statistics.service.ProductClickLogService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.github.f4b6a3.ulid.UlidCreator;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
	private final BookingMapper bookingMapper;
	private final ProductService productService;
	private final BranchService branchService;
	private final ProductClickLogService clickLogService;

	// ------------------- 조회 관련 메서드 -------------------

	/**
	 * 사용자의 이메일로 예약 목록 조회
	 * @param email 사용자 이메일
	 * @return 예약 DTO 리스트
	 */
	@Override
	public List<BookingDto> getBookingsByEmail(String email) {
		deletePastBookings(email);
		return bookingMapper.getBookingsByEmail(email).stream()
			.map(BookingDto::of)
			.collect(Collectors.toList());
	}

	/**
	 * 예약 ID로 예약 상세 조회
	 * @param bookingId 예약 ULID
	 * @return 예약 상세 응답 DTO
	 * @throws NoSuchElementException 예약이 없으면 발생
	 */
	@Override
	public BookingDetailResponseDto getBookingById(String bookingId) {
		BookingVo bookingVo = bookingMapper.findById(bookingId);

		if (bookingVo == null) {
			throw new NoSuchElementException("Booking not found with ulid: " + bookingId);
		}

		// 시간 초단위 제거 (ex: "10:00:00" → "10:00")
		String time = bookingVo.getTime();
		if (time != null && time.length() > 5) {
			bookingVo.setTime(time.substring(0, 5));
		}

		String prdtName = productService.getProductNameByCode(bookingVo.getFinPrdtCode());
		String branchName = branchService.getBranchNameById(bookingVo.getBranchId());

		return BookingDetailResponseDto.of(bookingVo, prdtName, branchName);
	}

	/**
	 * 식별자(ULID 또는 예약 코드)와 요청자 이메일로 예약 상세 정보를 조회합니다.
	 * @param identifier ULID 또는 예약 코드
	 * @param requesterEmail 조회를 요청한 사용자의 이메일 (권한 확인용)
	 * @return 예약 상세 응답 DTO
	 * @throws NoSuchElementException 예약이 없으면 발생
	 * @throws AccessDeniedException 조회 권한이 없으면 발생
	 */
	@Override
	public BookingDetailResponseDto getBookingDetailByIdentifier(String identifier, String requesterEmail) {
		BookingVo bookingVo;

		// 1. 식별자로 예약 정보 조회 (기존과 동일)
		if (identifier != null && identifier.contains("-")) {
			bookingVo = bookingMapper.findByBookingCode(identifier);
		} else {
			bookingVo = bookingMapper.findById(identifier);
		}

		if (bookingVo == null) {
			throw new NoSuchElementException("Booking not found with identifier: " + identifier);
		}

		// 2. 권한 확인: 요청자 이메일과 예약된 이메일이 동일한지 비교
		if (!requesterEmail.equals(bookingVo.getEmail())) {
			throw new UserAccessDeniedException("해당 예약을 조회할 권한이 없습니다.");
		}

		// 3. 권한 확인 통과 후, DTO로 변환하여 반환
		String time = bookingVo.getTime();
		if (time != null && time.length() > 5) {
			bookingVo.setTime(time.substring(0, 5));
		}

		String prdtName = productService.getProductNameByCode(bookingVo.getFinPrdtCode());
		String branchName = branchService.getBranchNameById(bookingVo.getBranchId());

		return BookingDetailResponseDto.of(bookingVo, prdtName, branchName);
	}

	/**
	 * 특정 지점(branchId)의 예약된 시간 슬롯 목록 조회
	 * @param branchId 지점 번호
	 * @return 예약된 슬롯 정보 DTO (날짜별 시간 리스트 맵)
	 */
	@Override
	public ReservedSlotsResponseDto getReservedSlotsByBranch(int branchId) {
		String currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));

		List<BookingVo> futureBookings = bookingMapper.findFutureByBranch(branchId, currentTime);

		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		Map<String, List<String>> reservedSlotsMap = futureBookings.stream()
			.collect(Collectors.groupingBy(
				booking -> booking.getDate().toInstant()
					.atZone(ZoneId.systemDefault())
					.toLocalDate()
					.format(dateFormatter),
				Collectors.mapping(
					booking -> {
						String time = booking.getTime();
						return (time != null && time.length() >= 5) ? time.substring(0, 5) : time;
					},
					Collectors.toList()
				)
			));

		return new ReservedSlotsResponseDto(reservedSlotsMap);
	}

	/**
	 * 특정 사용자(email)가 특정 상품(finPrdtCode)에 대해 예약 존재 여부 확인
	 * @param email 사용자 이메일
	 * @param finPrdtCode 금융 상품 코드
	 * @return 예약 존재 여부 및 상세 정보가 담긴 DTO
	 */
	public BookingCheckResponseDto checkBookingExists(String email, String finPrdtCode) {
		BookingVo existingBooking = bookingMapper.findByEmailAndFinPrdtCode(email, finPrdtCode);
		Optional<BookingVo> existingBookingOpt = Optional.ofNullable(existingBooking);

		if (existingBookingOpt.isPresent()) {
			BookingCheckDetailDto detailDto = BookingCheckDetailDto.from(existingBookingOpt.get());
			return new BookingCheckResponseDto(true, detailDto);
		} else {
			return new BookingCheckResponseDto(false, null);
		}
	}

	// ------------------- 예약 생성 -------------------

	/**
	 * 신규 예약 생성
	 * @param email 예약자 이메일
	 * @param requestDto 예약 생성 요청 DTO
	 * @return 생성된 예약의 응답 DTO
	 * @throws DuplicateBookingException 이미 예약이 존재할 경우 예외 발생
	 * @throws InvalidBookingDateException 예약 날짜가 유효하지 않을 경우 예외 발생
	 */
	@Override
	// [수정] 트랜잭션 처리를 위해 @Transactional 애너테이션 추가 (필수)
	public BookingCreateResponseDto addBooking(String email, BookingCreateRequestDto requestDto) {
		BookingVo bookingVo = requestDto.toVo();

		validateBookingDate(bookingVo.getDate());

		// 동시성 문제를 피하기 위해 예약 생성 로직 안에서 중복 체크를 다시 하거나, DB의 UNIQUE 제약조건에 의존하는 것이 더 안전할 수 있습니다.
		// 기존 로직은 유지하되, 트랜잭션의 격리 수준(isolation level)에 따라 동작이 달라질 수 있음을 인지해야 합니다.
		int existingBookings = bookingMapper.countByBranchDateTime(
			bookingVo.getBranchId(),
			bookingVo.getDate(),
			bookingVo.getTime()
		);

		if (existingBookings > 0) {
			throw new DuplicateBookingException("해당 지점의 해당 시간에는 이미 예약이 존재합니다.");
		}

		// 1. 내부용 ID (ULID) 생성 (기존과 동일)
		String bookingId = UlidCreator.getUlid().toString();

		// 2. 외부 공개용 예약 코드 생성
		// 2-1. 날짜를 'yyMMdd' 형식으로 변환
		String datePart = new SimpleDateFormat("yyMMdd").format(bookingVo.getDate());

		// 2-2. 지점 ID로 지점 코드(예: "GN")를 조회하는 로직 (별도 구현 필요)
		// 여기서는 예시로 "BRANCH" + branchId를 사용합니다.
		String branchPart = "B" + String.format("%03d", bookingVo.getBranchId());

		// 2-3. 해당 날짜, 해당 지점의 다음 순번 계산 (매퍼 호출)
		int dailySequence = bookingMapper.countByBranchAndDate(bookingVo.getBranchId(), bookingVo.getDate()) + 1;
		String sequencePart = String.format("%03d", dailySequence); // 3자리로 패딩 (001, 002...)

		// 2-4. 모든 부분을 조합하여 최종 예약 코드 생성
		String bookingCode = String.format("%s-%s-%s", datePart, branchPart, sequencePart); // 예: 250810-B01-001

		// 3. 생성된 ID와 코드를 Vo에 모두 설정
		DocInfoDto initialDocInfo = generateInitialDocInfo(requestDto.getFinPrdtCode());
		bookingVo.setEmail(email);
		bookingVo.setDocInfo(initialDocInfo);
		bookingVo.setBookingId(bookingId);       // 내부용 ID 설정
		bookingVo.setBookingCode(bookingCode);   // 외부용 코드 설정

		// 4. 데이터베이스에 최종 예약 정보 삽입
		bookingMapper.insertBooking(bookingVo);
		clickLogService.saveClickLog(requestDto.getFinPrdtCode(), email, "/api/bookings");

		// 5. 생성된 예약 정보를 DTO에 담아 반환
		return BookingCreateResponseDto.of(bookingVo);
	}

	@Override
	public void sendBookingToBank(String email, BookingCreateRequestDto requestDto,
		BookingCreateResponseDto responseDto) {
		try {
			RestTemplate restTemplate = new RestTemplate();
			String bankApiUrl = "http://localhost:8000/api/bookings"; // 은행 서버 endpoint

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			// String -> java.sql.Date 변환
			java.sql.Date sqlDate = java.sql.Date.valueOf(requestDto.getDate());

			// 은행 서버 DTO 생성
			BankBookingRequestDto bankDto = BankBookingRequestDto.builder()
				.email(email)
				.branchId(requestDto.getBranchId())
				.finPrdtCode(requestDto.getFinPrdtCode())
				.date(java.sql.Date.valueOf(requestDto.getDate()))
				.time(requestDto.getTime())
				.docInfo(responseDto.getDocInfo())
				.build();

			HttpEntity<BankBookingRequestDto> requestEntity = new HttpEntity<>(bankDto, headers);
			restTemplate.postForEntity(bankApiUrl, requestEntity, Void.class);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ------------------- 예약 수정 -------------------

	/**
	 * 예약 정보 부분 수정 (날짜, 시간)
	 * @param bookingId 수정할 예약 ID
	 * @param email 수정 요청자 이메일 (권한 확인용)
	 * @param patchDto 수정 요청 DTO (변경할 필드 포함)
	 * @return 수정된 예약 상세 응답 DTO
	 * @throws AccessDeniedException 권한 없을 시 예외 발생
	 * @throws DuplicateBookingException 중복 예약 시 예외 발생
	 * @throws NoSuchElementException 예약이 없을 경우 예외 발생
	 */
	@Transactional
	@Override
	public BookingDetailResponseDto patchBooking(String bookingId, String email,
		BookingPatchRequestDto patchDto) throws AccessDeniedException {

		BookingVo existingBooking = bookingMapper.findById(bookingId);

		if (existingBooking == null) {
			throw new NoSuchElementException("해당 예약을 찾을 수 없습니다: " + bookingId);
		}

		if (!existingBooking.getEmail().equals(email)) {
			throw new AccessDeniedException("예약을 수정할 권한이 없습니다.");
		}

		boolean isTimeSlotChanged = false;

		if (patchDto.getDate() != null && !patchDto.getDate().isEmpty()) {
			Date newDate = parseDate(patchDto.getDate());
			validateBookingDate(newDate);
			existingBooking.setDate(newDate);
			isTimeSlotChanged = true;
		}

		if (patchDto.getTime() != null && !patchDto.getTime().isEmpty()) {
			existingBooking.setTime(patchDto.getTime());
			isTimeSlotChanged = true;
		}

		if (isTimeSlotChanged) {
			int existingCount = bookingMapper.countByBranchDateTime(
				existingBooking.getBranchId(),
				existingBooking.getDate(),
				existingBooking.getTime()
			);
			if (existingCount > 0) {
				throw new DuplicateBookingException("변경하려는 시간에는 이미 다른 예약이 존재합니다.");
			}
		}

		bookingMapper.updateBooking(existingBooking);

		String prdtName = productService.getProductNameByCode(existingBooking.getFinPrdtCode());
		String branchName = branchService.getBranchNameById(existingBooking.getBranchId());

		return BookingDetailResponseDto.of(existingBooking, prdtName, branchName);
	}

	// ------------------- 예약 삭제 -------------------

	/**
	 * 예약 삭제
	 * @param bookingId 삭제할 예약 ID
	 * @param currentUserEmail 삭제 요청자 이메일 (권한 확인용)
	 * @throws AccessDeniedException 권한 없을 시 예외 발생
	 * @throws NoSuchElementException 예약이 없을 경우 예외 발생
	 */
	@Transactional
	@Override
	public void deleteBooking(String bookingId, String currentUserEmail) throws AccessDeniedException {
		BookingVo bookingVo = bookingMapper.findById(bookingId);

		if (bookingVo == null) {
			throw new NoSuchElementException("해당 예약을 찾을 수 없습니다: " + bookingId);
		}

		if (!bookingVo.getEmail().equals(currentUserEmail)) {
			throw new AccessDeniedException("예약을 삭제할 권한이 없습니다.");
		}

		bookingMapper.deleteBooking(bookingId);
	}

	// ------------------- 헬퍼 메서드 -------------------

	/**
	 * 사용자의 지난 예약 내역을 삭제합니다.
	 * @param email 사용자 이메일
	 */
	private void deletePastBookings(String email) {
		LocalDate today = LocalDate.now();
		bookingMapper.deletePastBookingsByEmail(email, today);
	}

	/**
	 * 문자열을 Date 객체로 파싱 (yyyy-MM-dd 형식)
	 * @param dateString 파싱할 문자열
	 * @return Date 객체
	 * @throws IllegalArgumentException 형식이 올바르지 않으면 예외 발생
	 */
	private Date parseDate(String dateString) {
		try {
			return new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
		} catch (ParseException e) {
			throw new IllegalArgumentException("Invalid date format. Please use yyyy-MM-dd.", e);
		}
	}

	/**
	 * 상품 코드에 따른 초기 필요 서류 목록 생성
	 * @param prdtCode 상품 코드
	 * @return DocInfoDto 객체 (필요 서류 포함)
	 */
	public DocInfoDto generateInitialDocInfo(String prdtCode) {
		DocInfoDto docInfo = new DocInfoDto();
		List<String> requiredDocs = new ArrayList<>();

		if (prdtCode == null || prdtCode.isEmpty()) {
			requiredDocs.add("신분증 (필수)");
			docInfo.setRequiredDocuments(requiredDocs);
			return docInfo;
		}

		if (prdtCode.startsWith("LN")) {
			requiredDocs.add("신분증");
			requiredDocs.add("주민등록등본");
			requiredDocs.add("가족관계증명서");
			requiredDocs.add("인감증명서 및 인감도장");
			requiredDocs.add("소득증빙서류 (재직/사업자 유형에 따라 상이)");
			requiredDocs.add("부동산 등기권리증 또는 매매계약서");
			requiredDocs.add("건축물대장");
		} else if (prdtCode.startsWith("DP")) {
			requiredDocs.add("신분증");
		} else if ("gift".equals(prdtCode)) {
			// 증여 필요 서류
			requiredDocs.add("실명확인증");
			requiredDocs.add("가족관계증명서");
			requiredDocs.add("인감증명서 및 인감도장");
		} else if ("inheritance".equals(prdtCode)) {
			// 상속 필요 서류
			requiredDocs.add("실명확인증");
			requiredDocs.add("피상속인의 가족관계증명서");
			requiredDocs.add("피상속인의 기본증명서");
			requiredDocs.add("인감증명서");
		} else {
			requiredDocs.add("신분증");
			requiredDocs.add("상품가입 관련 추가서류 (필요시)");
		}

		docInfo.setRequiredDocuments(requiredDocs);
		return docInfo;
	}

	/**
	 * 예약 날짜 유효성 검사 (오늘부터 한 달 이내만 허용)
	 * @param bookingDate 검사할 예약 날짜
	 * @throws InvalidBookingDateException 날짜가 범위 밖일 경우 예외 발생
	 */
	private void validateBookingDate(Date bookingDate) {
		if (bookingDate == null) {
			throw new InvalidBookingDateException("예약 날짜를 입력해주세요.");
		}

		LocalDate today = LocalDate.now();
		LocalDate requestedDate = bookingDate.toInstant()
			.atZone(ZoneId.systemDefault())
			.toLocalDate();

		LocalDate oneMonthLater = today.plusMonths(1);

		if (requestedDate.isBefore(today) || requestedDate.isAfter(oneMonthLater)) {
			throw new InvalidBookingDateException("예약은 오늘부터 한 달 이내의 날짜만 가능합니다.");
		}
	}
}
