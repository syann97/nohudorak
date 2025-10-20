package org.scoula.statistics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.scoula.statistics.dto.ProductClickStatsDto;
import org.scoula.statistics.mapper.ProductClickLogMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * ProductClickStatsService 구현체
 *
 * - 클릭 로그 집계 및 은행 서버 전송
 * - 특정 시점 이후 통계 조회
 * - 이전 로그 삭제
 */
@Service
@RequiredArgsConstructor
public class ProductClickStatsServiceImpl implements ProductClickStatsService {

	private final ProductClickLogMapper logMapper;
	private final BankServerApiClient bankApiClient;

	/**
	 * 1일 전부터 현재까지 클릭 로그를 집계하여 은행 서버로 전송
	 */
	@Override
	public void sendStatsToBank() {
		LocalDateTime fromDate = LocalDateTime.now().minusDays(1); // 기준 날짜: 1일 전

		List<ProductClickStatsDto> stats = getClickStatsSince(fromDate);

		if (!stats.isEmpty()) {
			bankApiClient.sendClickStats(stats); // 은행 서버 API 호출
		}
	}

	/**
	 * 특정 시점 이후 클릭 로그 통계 조회
	 *
	 * @param fromDate 조회 시작 시점 (포함)
	 * @return ProductClickStatsDto 리스트
	 */
	@Override
	public List<ProductClickStatsDto> getClickStatsSince(LocalDateTime fromDate) {
		return logMapper.selectClickStatsSince(fromDate);
	}

	/**
	 * 특정 시점 이전의 클릭 로그 삭제
	 *
	 * @param toDate 삭제 기준 시점 (이전 데이터 모두 삭제)
	 */
	@Override
	public void deleteClickLogsBefore(LocalDateTime toDate) {
		logMapper.deleteClickLogsBefore(toDate);
	}
}
