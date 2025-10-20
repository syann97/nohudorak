package org.scoula.user.dto;

import org.scoula.user.domain.UserVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "사용자 요약 정보 DTO (이름만 포함)")
@Builder
public class UserInfoDto {

	@ApiModelProperty(value = "사용자 이름", example = "홍길동", required = true)
	private String userName;

	/**
	 * UserVo 객체에서 UserInfoDto를 생성합니다.
	 * @param userVO UserVo 객체
	 * @return 생성된 UserInfoDto 객체
	 */
	public static UserInfoDto of(UserVo userVO) {
		return new UserInfoDto(
			userVO.getUserName()
		);
	}
}
