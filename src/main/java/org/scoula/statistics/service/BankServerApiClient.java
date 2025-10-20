package org.scoula.statistics.service;

import java.util.List;

import org.scoula.statistics.dto.BookingStatsDto;
import org.scoula.statistics.dto.ProductClickStatsDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * 은행서버와의 통신을 위한 service
 */
@Log4j2
@Component
@RequiredArgsConstructor
public class BankServerApiClient {

	private final RestTemplate restTemplate;

	private final String bankApiUrl = "http://localhost:8000/api";

	/**
	 * `/click-stats` - 상품 예약하기를 클릭했을 시 발생
	 * @param stats
	 */
	public void sendClickStats(List<ProductClickStatsDto> stats) {
		restTemplate.postForEntity(bankApiUrl + "/click-stats", stats, Void.class);
	}

	/**
	 * '/booking-stats' - 추후 예약 관련 통계 모두 사용할 가능성을 두고 API는 booking으로 설정
	 * @param stats 예약 통계 리스트
	 */
	public void sendBookingStats(List<BookingStatsDto> stats) {
		ResponseEntity<Void> response =
			restTemplate.postForEntity(bankApiUrl + "/booking-stats", stats, Void.class);
	}
}
