package org.scoula.gift.dto;

import org.scoula.asset.domain.AssetStatusVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "증여 페이지용 개별 자산 DTO", description = "자산의 ID, 이름, 금액을 담는 객체")
public class GiftAssetDto {

	@ApiModelProperty(value = "자산 고유 ID", example = "101")
	private int assetId;

	@ApiModelProperty(value = "자산명", example = "신한은행 주거래통장")
	private String assetName;

	@ApiModelProperty(value = "자산 금액", example = "50000000")
	private Long amount;

	public static GiftAssetDto of(AssetStatusVo vo) {
		return GiftAssetDto.builder()
			.assetId(vo.getAssetId())
			.assetName(vo.getAssetName())
			.amount(vo.getAmount())
			.build();
	}
}
