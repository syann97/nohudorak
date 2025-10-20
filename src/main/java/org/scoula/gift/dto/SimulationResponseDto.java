package org.scoula.gift.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 증여 시뮬레이션 결과 응답 DTO
 */
@ApiModel(value = "증여 시뮬레이션 응답 DTO", description = "총 예상 증여세, 수증자별 세금 상세 정보, 절세 전략 정보를 담습니다.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimulationResponseDto {

	@ApiModelProperty(value = "총 예상 증여세액", example = "1000000", required = true)
	private long totalEstimatedTax;

	@ApiModelProperty(value = "수증자별 세금 상세 정보 리스트", required = true)
	private List<RecipientTaxDetailDto> recipientDetails;

	@ApiModelProperty(value = "추천 절세 전략 리스트", required = true)
	private List<StrategyResponseDto> taxSavingStrategies;
}
