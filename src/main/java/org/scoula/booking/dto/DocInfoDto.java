package org.scoula.booking.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "서류 정보 DTO", description = "예약 시 필요한 서류 정보를 담는 객체")
public class DocInfoDto {

	@ApiModelProperty(
		value = "필요한 서류 목록",
		example = "[\"신분증 사본\", \"주민등록등본\"]"
	)
	private List<String> requiredDocuments;
}
