package org.scoula.statistics.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.statistics.dto.BookingStatsDto;

/**
 * 예약 테이블 집계 데이터를 조회하기 위한 Mapper 인터페이스
 * DB의 예약 관련 데이터를 집계하여 DTO로 반환합니다.
 */
@Mapper
public interface BookingStatsMapper {

	/**
	 * 특정 기간 동안의 예약 통계를 조회합니다.
	 *
	 * @param params 조회 조건을 담은 Map
	 *               예: "startDate" -> LocalDateTime, "endDate" -> LocalDateTime
	 * @return 기간 내 지점별 예약 통계 리스트
	 */
	List<BookingStatsDto> selectBookingStatsBetween(Map<String, Object> params);
}
