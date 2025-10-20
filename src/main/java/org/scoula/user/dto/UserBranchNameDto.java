package org.scoula.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "사용자 지점명 응답 DTO")
public class UserBranchNameDto {

	@ApiModelProperty(value = "지점 이름", example = "강남점")
	private String branchName;
}
