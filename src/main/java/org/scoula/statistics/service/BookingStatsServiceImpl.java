package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scoula.statistics.dto.BookingStatsDto;
import org.scoula.statistics.mapper.BookingStatsMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 예약 통계 집계 관련 서비스 구현체
 *
 * - BookingStatsMapper를 사용하여 DB에서 예약 통계 데이터를 조회
 */
@Service
@RequiredArgsConstructor
public class BookingStatsServiceImpl implements BookingStatsService {

	private final BookingStatsMapper bookingStatsMapper;

	/**
	 * 지정된 기간 동안의 예약 통계 데이터를 조회
	 *
	 * @param from 시작일시 (포함)
	 * @param to   종료일시 (포함)
	 * @return BookingStatsDto 리스트
	 */
	@Override
	public List<BookingStatsDto> getBookingStatsBetween(LocalDateTime from, LocalDateTime to) {
		// Mapper에 전달할 파라미터 맵 생성
		Map<String, Object> params = new HashMap<>();
		params.put("fromDate", from);
		params.put("toDate", to);

		// DB 조회 후 리스트 반환
		return bookingStatsMapper.selectBookingStatsBetween(params);
	}
}
