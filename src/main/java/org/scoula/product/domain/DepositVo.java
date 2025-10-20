package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 예금 상품 정보를 담는 VO 클래스
 */
@ApiModel(value = "DepositVo", description = "예금 상품 상세 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DepositVo extends ProductVo<DepositOptionVo> {

	@ApiModelProperty(value = "만기 후 이자율", example = "1.5%")
	private String mtrtInt;

	@ApiModelProperty(value = "우대 조건", example = "급여이체 필수")
	private String spclCnd;

	@ApiModelProperty(value = "가입 제한", notes = "1: 제한없음, 2: 서민전용, 3: 일부제한", example = "1")
	private String joinDeny;

	@ApiModelProperty(value = "가입 대상", example = "만 19세 이상")
	private String joinMember;

	@ApiModelProperty(value = "가입 금액", example = "1000000")
	private String joinPrice;

	@ApiModelProperty(value = "가입 기간", example = "12개월")
	private String joinTerm;

	@ApiModelProperty(value = "기타 유의사항", example = "중도 해지 시 이자율 변동")
	private String etcNote;
}
