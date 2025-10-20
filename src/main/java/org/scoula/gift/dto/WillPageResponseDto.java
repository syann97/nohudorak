package org.scoula.gift.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * 유언장 템플릿 페이지에 필요한 사용자 정보를 전달하는 DTO
 */
@ApiModel(value = "유언장 페이지 사용자 정보 DTO", description = "유언장 템플릿 페이지에 필요한 사용자의 이메일, 이름, 생년월일 정보를 담습니다.")
@Getter
@Builder
public class WillPageResponseDto {

	@ApiModelProperty(value = "사용자 이메일", example = "user@example.com", required = true)
	private final String email;

	@ApiModelProperty(value = "사용자 이름", example = "홍길동", required = true)
	private final String name;

	@ApiModelProperty(value = "사용자 생년월일", example = "1990-01-01", required = true)
	private final String birth;
}
