package org.scoula.auth.dto;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@ApiModel(value = "리프레시 토큰 DTO", description = "서버 내부에서 관리되는 리프레시 토큰의 정보")
@NoArgsConstructor  // 기본 생성자 추가
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 추가
public class RefreshTokenDto {

	@ApiModelProperty(value = "토큰 소유자의 이메일", required = true, example = "test@example.com")
	private String email;

	@ApiModelProperty(value = "리프레시 토큰 값", required = true, example = "eyJhbGciOiJIUzUxMiJ9...")
	private String tokenValue;

	@ApiModelProperty(value = "토큰 만료 일시", required = true, example = "2025-08-08T15:00:00")
	private LocalDateTime expiresAt;
}
