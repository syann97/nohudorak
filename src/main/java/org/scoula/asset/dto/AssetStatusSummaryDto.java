package org.scoula.asset.dto;

import org.scoula.asset.domain.AssetStatusVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 노후 메인 페이지에서 보여질 자산현황에 필요한 데이터만 담은 DTO입니다.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "자산 현황 요약 DTO", description = "노후 메인 페이지의 자산 현황 요약 정보 객체")
public class AssetStatusSummaryDto {

	@ApiModelProperty(value = "자산 카테고리 코드", example = "01")
	private String assetCategoryCode;

	@ApiModelProperty(value = "해당 카테고리의 총액", example = "75000000")
	private Long amount;

	public static AssetStatusSummaryDto of(AssetStatusVo assetStatus) {
		return AssetStatusSummaryDto.builder()
			.assetCategoryCode(assetStatus.getAssetCategoryCode())
			.amount(assetStatus.getAmount())
			.build();
	}
}