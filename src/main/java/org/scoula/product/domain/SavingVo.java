package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * vo.Saving(적금) 정보
 */
@ApiModel(value = "SavingVo", description = "적금 상품 상세 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SavingVo extends ProductVo<SavingOptionVo> {

	@ApiModelProperty(value = "만기 후 이자율", example = "2.5%")
	private String mtrtInt;

	@ApiModelProperty(value = "우대 조건", example = "급여이체 시 우대금리 적용")
	private String spclCnd;

	@ApiModelProperty(value = "가입 제한 (1:제한없음, 2:서민전용, 3:일부제한)", example = "1")
	private String joinDeny;

	@ApiModelProperty(value = "가입 대상", example = "모든 고객")
	private String joinMember;

	@ApiModelProperty(value = "가입 금액", example = "100000")
	private String joinPrice;

	@ApiModelProperty(value = "가입 기간", example = "12개월")
	private String joinTerm;

	@ApiModelProperty(value = "기타 유의사항", example = "중도 해지 시 원금 손실 가능")
	private String etcNote;
}
