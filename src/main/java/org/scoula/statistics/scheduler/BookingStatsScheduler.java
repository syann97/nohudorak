package org.scoula.statistics.scheduler;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.scoula.statistics.dto.BookingStatsDto;
import org.scoula.statistics.service.BankServerApiClient;
import org.scoula.statistics.service.BookingStatsService;
import org.scoula.statistics.service.StatsSendHistoryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 통계 데이터를 매월 은행 서버로 전송하는 스케줄러
 *
 * - 매월 1일 00시에 실행
 * - 전월 1일부터 말일까지의 예약 통계를 집계 후 은행 서버로 전송
 * - 전송 후 StatsSendHistory에 전송 시각 기록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BookingStatsScheduler {

	private final BookingStatsService bookingStatsService;
	private final BankServerApiClient bankServerApiClient;
	private final StatsSendHistoryService historyService;

	/**
	 * 전월 예약 통계를 은행 서버로 전송
	 *
	 * 1. 전월 1일부터 말일까지의 기간 계산
	 * 2. 해당 기간의 예약 통계 조회
	 * 3. 통계 데이터가 존재하면 은행 서버로 전송
	 * 4. 전송 완료 시 StatsSendHistory 테이블에 기록 삽입
	 */
	@Transactional
	@Scheduled(cron = "0 0 0 1 * *") // 매월 1일 00시 실행
	public void sendBookingStats() {
		// 전월 1일 00시
		LocalDateTime startOfLastMonth = YearMonth.now()
			.minusMonths(1)
			.atDay(1)
			.atStartOfDay();

		// 전월 말일 23:59:59
		LocalDateTime endOfLastMonth = YearMonth.now()
			.minusMonths(1)
			.atEndOfMonth()
			.atTime(23, 59, 59);

		// 전월 예약 통계 조회
		List<BookingStatsDto> stats = bookingStatsService.getBookingStatsBetween(startOfLastMonth, endOfLastMonth);

		// 통계가 존재하면 은행 서버로 전송하고 전송 기록 삽입
		if (!stats.isEmpty()) {
			bankServerApiClient.sendBookingStats(stats);
			historyService.insertSentAt("BOOKING", LocalDateTime.now());
		}
	}
}
