package org.scoula.auth.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "로그인 응답 DTO", description = "로그인 성공 시 클라이언트에 반환하는 토큰 및 사용자 정보")
public class KakaoLoginResponseDto {

	@ApiModelProperty(value = "서버에서 발급한 Access Token", required = true, example = "eyJhbGciOiJIUzI1NiJ9...")
	private String accessToken;

	@ApiModelProperty(value = "서버에서 발급한 Refresh Token", required = true, example = "eyJhbGciOiJIUzUxMiJ9...")
	private String refreshToken;

	@ApiModelProperty(value = "사용자 고유 카카오 ID", required = true, example = "1")
	private String userEmail;

	@ApiModelProperty(value = "사용자 이름(닉네임)", required = true, example = "홍길동")
	private String userName;
}
