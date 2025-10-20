package org.scoula.recommend.service;

import java.util.List;
import org.scoula.recommend.dto.CustomRecommendDto;

/**
 * 맞춤 상품 추천 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface CustomRecommendService {

	/**
	 * 사용자 이메일로 맞춤 추천 상품 목록을 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return 맞춤 추천 상품 DTO 리스트
	 */
	List<CustomRecommendDto> getCustomRecommendsByEmail(String email);

	/**
	 * 사용자의 최신 정보(성향, 자산)를 기반으로 맞춤 추천 상품 목록을 갱신합니다.
	 * (기존 목록 삭제 후 새로 생성)
	 * @param email 추천 목록을 갱신할 사용자의 이메일
	 */
	void addCustomRecommend(String email);
}