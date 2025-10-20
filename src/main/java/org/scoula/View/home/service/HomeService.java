package org.scoula.View.home.service;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.scoula.View.home.dto.HomeResponseDto;
import org.scoula.View.home.dto.RecommendationDto;
import org.scoula.View.home.dto.UserSummary;
import org.scoula.booking.dto.BookingDetailResponseDto;
import org.scoula.booking.dto.BookingDto;
import org.scoula.booking.service.BookingService;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

/**
 * 홈 화면에 필요한 데이터를 조합하는 서비스 클래스
 */
@Service
@RequiredArgsConstructor
public class HomeService {
	private final UserService userService;
	private final BookingService bookingService;

	/**
	 * 로그인 상태에 따라 홈 화면에 필요한 데이터를 조회하고 조합하여 반환합니다.
	 * @param userEmail 로그인한 사용자의 이메일 (비로그인 시 null)
	 * @return 홈 화면 응답 DTO
	 */
	@Transactional
	public HomeResponseDto getHomeData(String userEmail) {
		// 추천 상품은 항상 포함 (현재는 하드코딩된 데이터 사용)
		List<RecommendationDto> hardcodedRecommends = List.of(
			new RecommendationDto("KB001", "KB Star 정기예금", "6개월 예치, 안정적인 이자 수익", 3.2,4.0),
			new RecommendationDto("KB002", "KB 골드 연금", "장기 투자 적합 연금 상품", 4.0,5.5),
			new RecommendationDto("KB003", "KB 투자형 펀드", "리스크 있지만 수익률 기대", 5.5,10.2)
		);

		if (userEmail == null) {
			// 비로그인 사용자는 추천 상품만 포함하여 리턴
			return HomeResponseDto.builder()
				.recommandTop3(hardcodedRecommends)
				.build();
		}

		// 로그인 사용자의 경우, 이름과 자산 정보를 조회하여 요약 DTO 생성
		UserSummary summary = UserSummary.builder()
			.name(userService.getUser(userEmail).getUserName())
			.asset(userService.getUser(userEmail).getAsset())
			.build();

		// 가장 가까운 예약 정보 조회
		BookingDetailResponseDto nearestBooking = findNearestBooking(userEmail);

		// 사용자 요약 정보와 추천 상품을 모두 포함하여 리턴
		return HomeResponseDto.builder()
			.userSummary(summary)
			.recommandTop3(hardcodedRecommends)
			.nearestBooking(nearestBooking)
			.build();
	}

	private BookingDetailResponseDto findNearestBooking(String userEmail) {
		List<BookingDto> bookings = bookingService.getBookingsByEmail(userEmail);
		Date now = new Date();

		Optional<BookingDto> nearestBooking = bookings.stream()
			.min(Comparator.comparing(BookingDto::getDate));

		return nearestBooking.map(bookingDto -> bookingService.getBookingById(bookingDto.getBookingId())).orElse(null);
	}
}
