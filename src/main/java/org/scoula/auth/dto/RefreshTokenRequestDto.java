package org.scoula.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Refresh Token을 재발급 받기 위한 Dto
 */
@Data
@ApiModel(value = "토큰 재발급 요청 DTO", description = "Access Token 재발급을 요청할 때 사용하는 DTO")
public class RefreshTokenRequestDto {
	@ApiModelProperty(value = "클라이언트가 보관하고 있던 Refresh Token", required = true, example = "eyJhbGciOiJIUzUxMiJ9...")
	private String refreshToken;
}
