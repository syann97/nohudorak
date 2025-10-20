package org.scoula.user.dto;

import java.text.SimpleDateFormat;

import org.scoula.user.domain.UserVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 사용자 기본 정보 조회 응답 DTO
 *
 * - UserVo 객체를 기반으로 API 응답 형태로 변환
 * - 생년월일(Date)을 "yyyy-MM-dd" 형식 문자열로 변환
 */
@Data
@Builder
@ApiModel(description = "사용자 기본 정보 조회 응답 DTO")
public class UserInfoResponseDto {

	@ApiModelProperty(value = "사용자 이메일", example = "user@example.com")
	private String email;

	@ApiModelProperty(value = "사용자 이름", example = "김스코")
	private String userName;

	@ApiModelProperty(value = "사용자 전화번호", example = "010-1234-5678")
	private String userPhone;

	@ApiModelProperty(value = "생년월일", example = "1995-08-07")
	private String birth; // Date -> String 변환

	/**
	 * UserVo -> UserInfoResponseDto 변환 메서드
	 *
	 * @param user 사용자 정보 VO
	 * @return UserInfoResponseDto 객체
	 */
	public static UserInfoResponseDto of(UserVo user) {
		String formattedBirth = null;
		if (user.getBirth() != null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			formattedBirth = sdf.format(user.getBirth());
		}

		return UserInfoResponseDto.builder()
			.email(user.getEmail())
			.userName(user.getUserName())
			.userPhone(user.getUserPhone())
			.birth(formattedBirth)
			.build();
	}
}
