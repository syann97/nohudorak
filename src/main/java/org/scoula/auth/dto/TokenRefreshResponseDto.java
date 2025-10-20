package org.scoula.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@ApiModel(value = "토큰 재발급 응답 DTO", description = "토큰 재발급 성공 시 새로 발급된 토큰 정보를 담은 DTO")
public class TokenRefreshResponseDto {

	@ApiModelProperty(value = "새로 발급된 Access Token", required = true, example = "eyJhbGciOiJIUzI1NiJ9...")
	private String accessToken;

	@ApiModelProperty(value = "새로 발급된 Refresh Token (정책에 따라 갱신될 수 있음)", required = true, example = "eyJhbGciOiJIUz...")
	private String refreshToken;
}
