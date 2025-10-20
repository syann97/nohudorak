package org.scoula.View.codef.dto;

import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "ConnectedId 생성 요청 DTO", description = "CODEF에 계정 연결을 요청할 때 사용하는 DTO")
public class ConnectedIdRequestDto {

	@ApiModelProperty(
		value = "연결할 금융기관 계정 목록",
		required = true,
		example = "[{\"countryCode\": \"KR\", \"businessType\": \"BK\", \"clientType\": \"P\", \"organization\": \"0004\", \"loginType\": \"ID\", \"id\": \"user_id\", \"password\": \"user_password\"}]"
	)
	private List<Map<String, Object>> accountList;
}
