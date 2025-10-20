package org.scoula.user.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(description = "지점 ID 수정 요청 DTO")
public class UserBranchIdDto {

	@ApiModelProperty(value = "지점 ID", example = "101", required = true)
	private Integer branchId;
}
