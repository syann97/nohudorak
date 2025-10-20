package org.scoula.user.dto;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(description = "사용자 기본 정보 수정 요청 DTO")
public class UserInfoUpdateRequestDto {

	@ApiModelProperty(value = "수정할 사용자 이름", example = "김새싹")
	private String userName;

	@ApiModelProperty(value = "수정할 휴대폰 번호", example = "010-9876-5432")
	private String userPhone;

	@ApiModelProperty(value = "수정할 생년월일", example = "1995-08-08")
	private Date birth;
}