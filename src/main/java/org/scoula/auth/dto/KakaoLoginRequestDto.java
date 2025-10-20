package org.scoula.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel(value = "카카오 로그인 요청 DTO")
public class KakaoLoginRequestDto {

	@ApiModelProperty(value = "카카오 인가 코드", required = true, example = "Abcde12345...")
	private String code;
}
