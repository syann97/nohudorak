package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * vo.Trust(신탁) 상품 정보
 */
@ApiModel(value = "TrustVo", description = "신탁 상품 상세 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TrustVo extends ProductVo<Object> {

	@ApiModelProperty(value = "기준가", example = "1000.0")
	private Double basePrice;

	@ApiModelProperty(value = "수익률", example = "3.5")
	private Double yieldRate;

	@ApiModelProperty(value = "펀드 유형", example = "공모형")
	private String fundType;

	@ApiModelProperty(value = "펀드 형태", example = "개별형")
	private String fundStructure;

	@ApiModelProperty(value = "세금 우대", example = "비과세")
	private String taxBenefit;

	@ApiModelProperty(value = "판매 시작일", example = "2025-01-01")
	private String saleStartDate;

	@ApiModelProperty(value = "신탁 보수", example = "0.5%")
	private String trustFee;

	@ApiModelProperty(value = "중도 해지 수수료", example = "1%")
	private String earlyTerminationFee;

	@ApiModelProperty(value = "예금자 보호 여부", example = "보호")
	private String depositProtection;
}
