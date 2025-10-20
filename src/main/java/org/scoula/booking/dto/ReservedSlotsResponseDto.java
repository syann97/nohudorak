package org.scoula.booking.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "예약 확인 응답 DTO", description = "예약 존재 여부 및 상세 정보를 포함하는 응답 데이터")
public class ReservedSlotsResponseDto {

	@ApiModelProperty(
		value = "예약된 슬롯 정보 (날짜별 시간 리스트)",
		example = "{\"2025-08-01\": [\"10:00\", \"11:00\"], \"2025-08-02\": [\"09:30\"]}"
	)
	@JsonProperty("reserved_slots")
	private Map<String, List<String>> reservedSlots;
}
