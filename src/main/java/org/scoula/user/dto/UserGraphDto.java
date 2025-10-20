package org.scoula.user.dto;

import java.util.List;

import org.scoula.asset.dto.AssetStatusSummaryDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "사용자 그래프 정보 DTO", description = "마이페이지의 그래프 표시에 필요한 사용자 이름과 자산 현황 데이터를 담는 DTO")
public class UserGraphDto {
	@ApiModelProperty(value = "사용자 이름", example = "홍길동")
	private String userName;

	@ApiModelProperty(value = "카테고리별 자산 현황 리스트")
	private List<AssetStatusSummaryDto> assetStatus;
}
