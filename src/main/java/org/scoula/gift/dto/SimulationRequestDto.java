package org.scoula.gift.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 증여 시뮬레이션 요청 DTO
 */
@ApiModel(value = "증여 시뮬레이션 요청 DTO", description = "수증자별 증여 내역을 담아 서버에 전달하는 요청 데이터")
@Data
public class SimulationRequestDto {

	@ApiModelProperty(value = "시뮬레이션 대상 수증자별 증여 정보 리스트", required = true)
	private List<RecipientGiftRequestDto> simulationList;
}
