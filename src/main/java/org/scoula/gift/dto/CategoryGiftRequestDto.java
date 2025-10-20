package org.scoula.gift.dto;

import java.util.List;

import lombok.Data;

@Data
public class CategoryGiftRequestDto {
	private String assetCategoryCode;
	private List<AssetGiftRequestDto> assets;
}