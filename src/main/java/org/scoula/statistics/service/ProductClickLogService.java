package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.scoula.statistics.dto.ProductClickStatsDto;

/**
 * 상품 클릭 로그 관련 서비스 인터페이스
 *
 * - 금융 상품 클릭 이벤트 기록
 * - 클릭 수 집계 조회
 */
public interface ProductClickLogService {

	/**
	 * 상품 클릭 이벤트를 저장
	 *
	 * @param finPrdtCd   클릭된 금융상품 코드
	 * @param email       클릭한 사용자 이메일
	 * @param triggeredBy 클릭 이벤트 발생 주체 (ex: UI, API 등)
	 */
	void saveClickLog(String finPrdtCd, String email, String triggeredBy);

	/**
	 * 지정된 날짜 이후 발생한 클릭 수 집계 조회
	 *
	 * @param fromDate 기준 날짜/시간
	 * @return ProductClickStatsDto 리스트
	 */
	List<ProductClickStatsDto> getClickStatsSince(LocalDateTime fromDate);
}
