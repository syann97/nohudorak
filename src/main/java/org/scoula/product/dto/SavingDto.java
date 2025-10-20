package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Saving(적금) DTO
 */
@ApiModel(value = "SavingDto", description = "적금 상품 상세 정보 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class SavingDto extends ProductDetailDto<SavingOptionDto> {

	@ApiModelProperty(value = "만기 후 이자율")
	private String mtrtInt;

	@ApiModelProperty(value = "우대조건")
	private String spclCnd;

	@ApiModelProperty(value = "가입제한 (1:제한없음, 2:서민전용, 3:일부제한)")
	private String joinDeny;

	@ApiModelProperty(value = "가입대상")
	private String joinMember;

	@ApiModelProperty(value = "가입금액")
	private String joinPrice;

	@ApiModelProperty(value = "가입기간")
	private String joinTerm;

	@ApiModelProperty(value = "기타 유의사항")
	private String etcNote;
}
