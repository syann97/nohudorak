package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 금 상품 정보를 담는 VO 클래스
 */
@ApiModel(value = "GoldVo", description = "금 상품 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class GoldVo extends ProductVo<Object> {

	@ApiModelProperty(value = "거래 단위 (0.01g)", example = "0.01")
	private String lot;

	@ApiModelProperty(value = "통화 단위 (원화 KRW)", example = "KRW")
	private String currency;

	@ApiModelProperty(value = "기타 유의 사항", example = "특별 우대 없음")
	private String etcNote;
}
