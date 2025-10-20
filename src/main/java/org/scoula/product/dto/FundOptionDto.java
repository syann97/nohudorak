package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * Fund(펀드) 옵션 DTO
 */
@ApiModel(value = "FundOptionDto", description = "펀드 옵션 정보를 담는 DTO")
@Builder
@Getter
public class FundOptionDto {

	@ApiModelProperty(value = "3개월 수익률")
	private String rate3mon;

	@ApiModelProperty(value = "시작일")
	private String startDate;

	@ApiModelProperty(value = "총자산")
	private String assetTotal;

	@ApiModelProperty(value = "총보수")
	private String totalFee;

	@ApiModelProperty(value = "위험등급")
	private String riskGrade;

	@ApiModelProperty(value = "선취수수료")
	private String feeFirst;

	@ApiModelProperty(value = "환매수수료")
	private String feeRedemp;

	@ApiModelProperty(value = "기준가")
	private String priceStd;

	// @ApiModelProperty(value = "투자성향")
	// private Double tendency;
}
