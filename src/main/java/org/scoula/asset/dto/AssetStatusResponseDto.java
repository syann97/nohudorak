package org.scoula.asset.dto;

import org.scoula.asset.domain.AssetStatusVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "자산 현황 응답 DTO", description = "자산 현황 조회 시 반환되는 데이터 객체")
public class AssetStatusResponseDto {

	@ApiModelProperty(value = "자산 ID", example = "1")
	private int assetId;

	@ApiModelProperty(value = "자산 카테고리 코드", example = "01")
	private String assetCategoryCode;

	@ApiModelProperty(value = "금액", example = "50000000")
	private Long amount;

	@ApiModelProperty(value = "자산명", example = "국민은행 주택청약")
	private String assetName;

	@ApiModelProperty(value = "거래 구분", example = "매수")
	private String businessType;

	public static AssetStatusResponseDto of(AssetStatusVo assetStatus) {
		return AssetStatusResponseDto.builder()
			.assetId(assetStatus.getAssetId())
			.assetCategoryCode(assetStatus.getAssetCategoryCode())
			.amount(assetStatus.getAmount())
			.assetName(assetStatus.getAssetName())
			.businessType(assetStatus.getBusinessType())
			.build();
	}

	public AssetStatusVo toVo() {
		return AssetStatusVo.builder()
			.assetId(assetId)
			.assetCategoryCode(assetCategoryCode)
			.amount(amount)
			.assetName(assetName)
			.businessType(businessType)
			.build();
	}
}