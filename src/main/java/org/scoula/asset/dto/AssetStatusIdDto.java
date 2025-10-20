package org.scoula.asset.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "자산 ID 응답 DTO", description = "자산 생성 후 반환되는 ID를 담는 객체")
public class AssetStatusIdDto {

	@ApiModelProperty(value = "생성된 자산의 고유 ID", example = "123", required = true)
	private int assetId;
}