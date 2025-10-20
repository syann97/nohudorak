package org.scoula.gift.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 절세 전략 정보 DTO
 */
@ApiModel(value = "절세 전략 DTO", description = "추천 절세 전략 정보를 담는 DTO")
@Data
@AllArgsConstructor
public class StrategyResponseDto {

	@ApiModelProperty(value = "전략 카테고리", example = "증여 한도 활용", required = true)
	private String ruleCategory;

	@ApiModelProperty(value = "전략 상세 내용", example = "수증자별 최대 한도를 고려하여 증여를 분할합니다.", required = true)
	private String content;
}
