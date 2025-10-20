package org.scoula.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(value = "카카오 토큰 응답 DTO", description = "카카오 인증 서버로부터 받은 토큰 정보를 담는 객체")
public class KakaoTokenResponseDto {

	@ApiModelProperty(value = "사용자 액세스 토큰 값", required = true, example = "o4aR_sx_...J6z5v5wAAAA")
	@JsonProperty("access_token")
	private String accessToken;

	@ApiModelProperty(value = "토큰 타입", required = true, example = "bearer")
	@JsonProperty("token_type")
	private String tokenType;

	@ApiModelProperty(value = "사용자 리프레시 토큰 값", required = true, example = "j9z-hW_..._pT41pC-QAA")
	@JsonProperty("refresh_token")
	private String refreshToken;

	@ApiModelProperty(value = "액세스 토큰 만료 시간 (초)", required = true, example = "21599")
	@JsonProperty("expires_in")
	private Integer expiresIn;

	@ApiModelProperty(value = "인증된 사용자의 정보 조회 권한 범위", example = "profile_image profile_nickname")
	@JsonProperty("scope")
	private String scope;

	@ApiModelProperty(value = "리프레시 토큰 만료 시간 (초)", required = true, example = "5184000")
	@JsonProperty("refresh_token_expires_in")
	private Integer refreshTokenExpiresIn;
}
