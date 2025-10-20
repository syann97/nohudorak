package org.scoula.booking.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "예약 확인 상세 DTO", description = "예약 확인 시 사용되는 상세 정보 객체")
public class BookingCheckResponseDto {

	@ApiModelProperty(value = "예약 존재 여부", example = "true")
	private boolean exists;

	@ApiModelProperty(value = "예약 상세 정보")
	private BookingCheckDetailDto bookingDetails;
}
