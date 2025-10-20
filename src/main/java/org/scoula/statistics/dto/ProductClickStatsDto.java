package org.scoula.statistics.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 상품 클릭 통계 데이터를 집계하여 은행 서버로 전송할 때 사용하는 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "ProductClickStatsDto", description = "상품 클릭 통계 데이터를 은행 서버로 전달하기 위한 DTO")
public class ProductClickStatsDto {

	@ApiModelProperty(value = "금융상품 코드", example = "DEP001")
	private String finPrdtCd;

	@ApiModelProperty(value = "클릭 수", example = "42")
	private Long clickCount;
}
