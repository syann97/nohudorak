package org.scoula.booking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "예약 수정 요청 DTO", description = "예약 수정을 위해 클라이언트에서 받는 데이터 (수정 가능한 필드만 포함)")
public class BookingPatchRequestDto {

	@ApiModelProperty(value = "변경할 예약 날짜 (yyyy-MM-dd)", example = "2025-08-01", required = false)
	private String date;

	@ApiModelProperty(value = "변경할 예약 시간 (HH:mm)", example = "15:30", required = false)
	private String time;
}
