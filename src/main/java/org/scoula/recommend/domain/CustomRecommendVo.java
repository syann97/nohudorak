package org.scoula.recommend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자별 맞춤 추천 상품 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomRecommendVo {
	/** 사용자 이메일 (FK) */
	private String userEmail;

	/** 추천된 금융 상품 코드 (FK) */
	private String finPrdtCd;

	/** 사용자와 상품 간의 적합도 점수 */
	private String score;
}