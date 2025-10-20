package org.scoula.gift.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "수증자 ID 응답 DTO", description = "수증자 생성 성공 시 생성된 ID를 반환하는 데이터")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientIdResponseDto {
	@ApiModelProperty(value = "생성된 수증자 ID", example = "17")
	private Integer recipientId;
}
