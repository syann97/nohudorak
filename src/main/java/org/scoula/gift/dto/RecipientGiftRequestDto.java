package org.scoula.gift.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 수증자별 증여 요청 데이터를 담는 DTO
 */
@ApiModel(value = "수증자별 증여 요청 DTO", description = "특정 수증자에게 어떤 카테고리 자산을 증여할지 요청 데이터를 담습니다.")
@Data
public class RecipientGiftRequestDto {

	@ApiModelProperty(value = "수증자 ID", example = "1", required = true)
	private Integer recipientId;

	@ApiModelProperty(value = "증여할 카테고리별 자산 정보 리스트", required = true)
	private List<CategoryGiftRequestDto> categoriesToGift;
}
