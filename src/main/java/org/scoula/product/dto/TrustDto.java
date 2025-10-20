package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Trust(신탁) DTO
 */
@ApiModel(value = "TrustDto", description = "신탁 상품 상세 정보 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class TrustDto extends ProductDetailDto<Object> {

	@ApiModelProperty(value = "기준가")
	private Double basePrice;

	@ApiModelProperty(value = "수익률")
	private Double yieldRate;

	@ApiModelProperty(value = "펀드유형")
	private String fundType;

	@ApiModelProperty(value = "펀드형태")
	private String fundStructure;

	@ApiModelProperty(value = "세금우대")
	private String taxBenefit;

	@ApiModelProperty(value = "판매시작일")
	private String saleStartDate;

	@ApiModelProperty(value = "신탁보수")
	private String trustFee;

	@ApiModelProperty(value = "중도해지수수료")
	private String earlyTerminationFee;

	@ApiModelProperty(value = "예금자보호 여부")
	private String depositProtection;
}
