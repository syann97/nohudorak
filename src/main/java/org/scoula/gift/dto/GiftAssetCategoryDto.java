package org.scoula.gift.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "증여 페이지용 자산 카테고리 DTO", description = "하나의 자산 카테고리와 총액, 그리고 그에 속한 자산 목록을 담는 객체")
public class GiftAssetCategoryDto {

	@ApiModelProperty(value = "자산 카테고리 코드", example = "02")
	private String assetCategoryCode;

	@ApiModelProperty(value = "해당 카테고리의 자산 총액", example = "75000000")
	private Long totalAmount;

	@ApiModelProperty(value = "해당 카테고리에 속한 개별 자산 목록")
	private List<GiftAssetDto> assets;
}
