package org.scoula.statistics.mapper;

import java.time.LocalDateTime;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 집계 데이터 은행 서버 전달 시 전송 기록(히스토리)을 관리하는 Mapper
 * DB의 stats_send_history 테이블과 매핑되며, 마지막 전송 시각 조회 및 전송 기록 삽입 기능을 제공합니다.
 */
@Mapper
public interface StatsSendHistoryMapper {

	/**
	 * 특정 통계 유형(statType)에 대해 마지막으로 전송된 시각을 조회합니다.
	 *
	 * @param statType 통계 유형 예: "BOOKING", "PRODUCT_CLICK"
	 * @return 마지막 전송 시각 (LocalDateTime), 기록이 없으면 null 반환
	 */
	LocalDateTime findLastSentAt(@Param("statType") String statType);

	/**
	 * 특정 통계 유형(statType)에 대한 전송 기록을 삽입합니다.
	 *
	 * @param statType 통계 유형
	 * @param sentAt 전송 시각
	 */
	void insertSentAt(@Param("statType") String statType, @Param("sentAt") LocalDateTime sentAt);
}
