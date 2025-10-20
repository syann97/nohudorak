package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.scoula.statistics.dto.BookingStatsDto;

public interface BookingStatsService {
	/**
	 * 지정된 기간의 예약 건수를 branch_id별로 집계해서 반환
	 * @param from 시작일
	 * @param to 종료일
	 * @return branch_id, booking_count 리스트
	 */
	List<BookingStatsDto> getBookingStatsBetween(LocalDateTime from, LocalDateTime to);
}
