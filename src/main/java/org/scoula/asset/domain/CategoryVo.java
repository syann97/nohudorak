package org.scoula.asset.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 자산 카테고리 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryVo {
	/** 자산 카테고리 코드 (PK) */
	private String assetCategoryCode;

	/** 카테고리명 (예: 예금, 주식) */
	private String name;
}