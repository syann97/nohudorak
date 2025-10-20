package org.scoula.statistics.service;

import java.time.LocalDateTime;

/**
 * 집계 데이터 전송 히스토리 관리 서비스 인터페이스
 *
 * - 은행 서버로 전송된 통계 데이터 기록 조회/등록
 */
public interface StatsSendHistoryService {

	/**
	 * 특정 통계 타입(statType)에 대해 마지막으로 전송된 시점 조회
	 *
	 * @param statType 통계 유형 (예: "BOOKING", "CLICK")
	 * @return 마지막 전송 시점, 전송 이력이 없으면 null 반환
	 */
	LocalDateTime findLastSentAt(String statType);

	/**
	 * 특정 통계 타입(statType)에 대해 전송 완료 시점 기록
	 *
	 * @param statType 통계 유형 (예: "BOOKING", "CLICK")
	 * @param sentAt 전송 완료 시점
	 */
	void insertSentAt(String statType, LocalDateTime sentAt);
}
