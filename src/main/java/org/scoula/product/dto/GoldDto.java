package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Gold(금) DTO
 */
@ApiModel(value = "GoldDto", description = "금 상품 상세 정보를 담는 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class GoldDto extends ProductDetailDto<Object> {

	@ApiModelProperty(value = "거래 단위 (예: 0.01g)")
	private String lot;

	@ApiModelProperty(value = "통화 단위 (예: KRW)")
	private String currency;

	@ApiModelProperty(value = "기타 유의사항")
	private String etcNote;
}
