package org.scoula.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 예약 테이블 데이터를 집계하여 은행 서버로 전달하기 위한 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "BookingStatsDto", description = "예약 집계 정보를 은행 서버로 전달하기 위한 DTO")
public class BookingStatsDto {

	@ApiModelProperty(value = "지점 ID", example = "101")
	private Long branchId;

	@ApiModelProperty(value = "예약 수", example = "25")
	private Long bookingCount;
}
