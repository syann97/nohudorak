package org.scoula.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자산 현황 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssetStatusVo {
	/** 자산 고유 ID */
	private int assetId;

	/** 사용자 이메일 (FK) */
	private String email;

	/** 자산 카테고리 코드 (FK) */
	private String assetCategoryCode;

	/** 금액 */
	private Long amount;

	/** 자산명 (예: 국민은행 주택청약) */
	private String assetName;

	/** 거래 구분 (예: 매수, 매도) */
	private String businessType;
}