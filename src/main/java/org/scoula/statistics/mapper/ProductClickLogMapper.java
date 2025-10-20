package org.scoula.statistics.mapper;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.statistics.domain.ProductClickLogVo;
import org.scoula.statistics.dto.ProductClickStatsDto;

/**
 * 상품 클릭 로그를 관리하는 Mapper 인터페이스
 * DB의 product_click_log 테이블과 매핑되며,
 * 클릭 로그 저장, 통계 집계, 오래된 로그 삭제 기능을 제공합니다.
 */
@Mapper
public interface ProductClickLogMapper {

	/**
	 * 클릭 로그를 DB에 저장합니다.
	 *
	 * @param log 클릭 로그 객체
	 */
	void insertClickLog(ProductClickLogVo log);

	/**
	 * 지정된 날짜 이후의 클릭 통계를 집계합니다.
	 *
	 * @param fromDate 조회 시작일(LocalDateTime)
	 * @return 상품별 클릭 수 리스트(ProductClickStatsDto)
	 */
	List<ProductClickStatsDto> selectClickStatsSince(@Param("fromDate") LocalDateTime fromDate);

	/**
	 * 지정된 날짜 이전의 클릭 로그를 삭제합니다.
	 *
	 * @param toDate 삭제 기준 날짜(LocalDateTime)
	 */
	void deleteClickLogsBefore(@Param("toDate") LocalDateTime toDate);
}
