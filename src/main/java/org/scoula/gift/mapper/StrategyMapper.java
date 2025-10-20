package org.scoula.gift.mapper;

import org.scoula.gift.domain.StrategyVo;
import java.util.List;

public interface StrategyMapper {
	/**
	 * 활성화된 모든 절세 전략 규칙을 조회합니다.
	 * (현재 테이블에는 is_active 컬럼이 없으므로 모든 규칙을 조회합니다.)
	 * @return 전략 규칙 VO 리스트
	 */
	List<StrategyVo> findAll();
}