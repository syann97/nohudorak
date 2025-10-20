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
@ApiModel(value = "자산 현황 생성/수정 요청 DTO", description = "자산 현황 생성 및 수정 시 사용되는 요청 데이터 객체")
public class AssetStatusRequestDto {

	@ApiModelProperty(value = "자산 카테고리 코드", example = "01", required = true)
	private String assetCategoryCode;

	@ApiModelProperty(value = "금액", example = "50000000", required = true)
	private Long amount;

	@ApiModelProperty(value = "자산명", example = "국민은행 주택청약", required = true)
	private String assetName;

	@ApiModelProperty(value = "거래 구분 (매수/매도)", example = "매수", required = true)
	private String businessType;

	public AssetStatusVo toVo() {
		return AssetStatusVo.builder()
			.assetCategoryCode(this.assetCategoryCode)
			.amount(this.amount)
			.assetName(this.assetName)
			.businessType(this.businessType)
			.build();
	}
}