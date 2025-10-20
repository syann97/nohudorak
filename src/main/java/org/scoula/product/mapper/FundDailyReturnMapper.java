package org.scoula.product.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.product.domain.FundDailyReturnVo;

/**
 * 펀드 일별 수익률 관련 Mapper 인터페이스
 */
@Mapper
public interface FundDailyReturnMapper {

	/**
	 * 특정 펀드 코드에 해당하는 일별 수익률 목록을 조회합니다.
	 *
	 * @param fundCode 조회할 펀드 코드
	 * @return 해당 펀드의 일별 수익률 목록
	 */
	List<FundDailyReturnVo> findByFundCode(@Param("fundCode") String fundCode);

}
