package org.scoula.View.home.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "사용자 요약 정보 DTO", description = "홈 화면에 표시될 로그인한 사용자의 요약 정보")
public class UserSummary {
	@ApiModelProperty(value = "사용자 이름", example = "홍길동")
	private String name;

	@ApiModelProperty(value = "사용자 총 자산", example = "150000000")
	private Long asset;
}