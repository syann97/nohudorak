package org.scoula.recommend.dto;

import org.scoula.recommend.domain.CustomRecommendVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "맞춤 추천 상품 DTO", description = "사용자에게 반환될 맞춤 추천 상품 정보")
public class CustomRecommendDto {
	@ApiModelProperty(value = "금융 상품 코드", example = "PRD001")
	private String finPrdtCd;

	@ApiModelProperty(value = "적합도 점수", example = "0.95")
	private String score;

	/**
	 * CustomRecommendVo 객체를 CustomRecommendDto로 변환합니다.
	 * @param customRecommend CustomRecommendVo 객체
	 * @return 변환된 CustomRecommendDto 객체
	 */
	public static CustomRecommendDto of(CustomRecommendVo customRecommend) {
		return CustomRecommendDto.builder()
			.finPrdtCd(customRecommend.getFinPrdtCd())
			.score(customRecommend.getScore())
			.build();
	}

	/**
	 * CustomRecommendDto 객체를 CustomRecommendVo로 변환합니다.
	 * @return 변환된 CustomRecommendVo 객체
	 */
	public CustomRecommendVo toVo() {
		return CustomRecommendVo.builder()
			.finPrdtCd(finPrdtCd)
			.score(score)
			.build();
	}
}