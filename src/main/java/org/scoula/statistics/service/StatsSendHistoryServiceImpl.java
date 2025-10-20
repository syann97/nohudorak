package org.scoula.statistics.service;

import java.time.LocalDateTime;

import org.scoula.statistics.mapper.StatsSendHistoryMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 집계 데이터 전송 히스토리 관리 서비스 구현체
 *
 * - 은행 서버로 전송된 통계 데이터의 마지막 전송 시점 조회 및 기록 등록 기능 제공
 */
@Service
@RequiredArgsConstructor
public class StatsSendHistoryServiceImpl implements StatsSendHistoryService {

	private final StatsSendHistoryMapper sendHistoryMapper;

	/**
	 * 특정 통계 타입(statType)에 대해 마지막 전송 시점 조회
	 *
	 * @param statType 통계 유형 (예: "BOOKING", "CLICK")
	 * @return 마지막 전송 시점, 전송 이력이 없으면 null 반환
	 */
	@Override
	public LocalDateTime findLastSentAt(String statType) {
		return sendHistoryMapper.findLastSentAt(statType);
	}

	/**
	 * 특정 통계 타입(statType)에 대해 전송 완료 시점 기록
	 *
	 * @param statType 통계 유형 (예: "BOOKING", "CLICK")
	 * @param sentAt 전송 완료 시점
	 */
	@Override
	public void insertSentAt(String statType, LocalDateTime sentAt) {
		sendHistoryMapper.insertSentAt(statType, sentAt);
	}
}
