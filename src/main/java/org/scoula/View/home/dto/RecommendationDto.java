package org.scoula.View.home.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "추천 상품 DTO", description = "홈 화면에 표시될 추천 상품의 요약 정보")
public class RecommendationDto {
	@ApiModelProperty(value = "금융 상품 코드", example = "KB001")
	private String fin_prdt_cd;

	@ApiModelProperty(value = "금융 상품명", example = "KB Star 정기예금")
	private String fin_prdt_nm;

	@ApiModelProperty(value = "상품 특징 요약", example = "6개월 예치, 안정적인 이자 수익")
	private String prdt_feature;

	@ApiModelProperty(value = "기본 금리", example = "3.2")
	private Double intr_rate;

	@ApiModelProperty(value = "최고 금리", example = "4.0")
	private Double intr_rate2;
}