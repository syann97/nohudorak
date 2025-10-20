package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.scoula.statistics.domain.ProductClickLogVo;
import org.scoula.statistics.dto.ProductClickStatsDto;
import org.scoula.statistics.mapper.ProductClickLogMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 상품 클릭 로그 서비스 구현체
 *
 * - 상품 클릭 이벤트 기록
 * - 클릭 수 집계 조회
 */
@Service
@RequiredArgsConstructor
public class ProductClickLogServiceImpl implements ProductClickLogService {

	private final ProductClickLogMapper logMapper;

	/**
	 * 상품 클릭 로그를 저장
	 *
	 * @param finPrdtCd   클릭된 금융상품 코드
	 * @param email       클릭한 사용자 이메일
	 * @param triggeredBy 클릭 이벤트 발생 주체 (ex: UI, API 등)
	 */
	@Override
	public void saveClickLog(String finPrdtCd, String email, String triggeredBy) {
		ProductClickLogVo log = new ProductClickLogVo();
		log.setFinPrdtCd(finPrdtCd);
		log.setEmail(email);
		log.setTriggeredBy(triggeredBy);
		logMapper.insertClickLog(log);
	}

	/**
	 * 특정 시점 이후 발생한 클릭 수 집계 조회
	 *
	 * @param fromDate 조회 기준 날짜/시간
	 * @return ProductClickStatsDto 리스트
	 */
	@Override
	public List<ProductClickStatsDto> getClickStatsSince(LocalDateTime fromDate) {
		return logMapper.selectClickStatsSince(fromDate);
	}
}
