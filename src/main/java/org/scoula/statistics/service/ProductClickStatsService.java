package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.scoula.statistics.dto.ProductClickStatsDto;

/**
 * 상품 클릭 통계 서비스 인터페이스
 *
 * - 클릭 로그 집계
 * - 은행 서버 전송
 * - 과거 로그 삭제
 */
public interface ProductClickStatsService {

	/**
	 * 일정 기간 이후 클릭 로그를 집계하여 은행 서버로 전송
	 *
	 * - 집계된 클릭 데이터는 은행 서버 API로 전달
	 */
	void sendStatsToBank();

	/**
	 * 특정 시점 이후의 클릭 로그 통계 집계 데이터 조회
	 *
	 * @param fromDate 조회 시작 시점 (포함)
	 * @return ProductClickStatsDto 리스트
	 */
	List<ProductClickStatsDto> getClickStatsSince(LocalDateTime fromDate);

	/**
	 * 특정 시점 이전의 클릭 로그 삭제
	 *
	 * @param toDate 삭제 기준 시점 (이전 데이터 모두 삭제)
	 */
	void deleteClickLogsBefore(LocalDateTime toDate);
}
