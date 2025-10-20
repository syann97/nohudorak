package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Deposit(예금) 상품 상세 DTO
 */
@ApiModel(value = "DepositDto", description = "예금 상품 상세 정보")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class DepositDto extends ProductDetailDto<DepositOptionDto> {

	@ApiModelProperty(value = "만기 후 이자율", example = "2.0%")
	private String mtrtInt;

	@ApiModelProperty(value = "우대조건", example = "최고 우대금리 적용")
	private String spclCnd;

	@ApiModelProperty(value = "가입제한", example = "1:제한없음, 2:서민전용, 3:일부제한")
	private String joinDeny;

	@ApiModelProperty(value = "가입대상", example = "만 19세 이상")
	private String joinMember;

	@ApiModelProperty(value = "가입금액", example = "1000000")
	private String joinPrice;

	@ApiModelProperty(value = "가입기간", example = "12개월")
	private String joinTerm;

	@ApiModelProperty(value = "기타 유의사항", example = "중도 해지 시 이자율 변동 가능")
	private String etcNote;
}
