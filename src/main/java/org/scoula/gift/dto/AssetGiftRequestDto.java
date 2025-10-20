package org.scoula.gift.dto;

import lombok.Data;

@Data
public class AssetGiftRequestDto {
	private Integer assetId;
	private Long giftAmount;
}