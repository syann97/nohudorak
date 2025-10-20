package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 펀드 옵션 정보를 담는 VO 클래스
 */
@ApiModel(value = "FundOptionVo", description = "펀드 옵션 정보")
@Data
public class FundOptionVo {

	@ApiModelProperty(value = "옵션 코드 (ID 역할)", example = "101")
	private Integer optionCd;

	@ApiModelProperty(value = "3개월 수익률", example = "0.035")
	private String rate3mon;

	@ApiModelProperty(value = "시작일", example = "2025-01-01")
	private String startDate;

	@ApiModelProperty(value = "총 자산", example = "100000000")
	private String assetTotal;

	@ApiModelProperty(value = "총 보수", example = "0.02")
	private String totalFee;

	@ApiModelProperty(value = "위험 등급", example = "3")
	private String riskGrade;

	@ApiModelProperty(value = "선취 수수료", example = "0.01")
	private String feeFirst;

	@ApiModelProperty(value = "환매 수수료", example = "0.005")
	private String feeRedemp;

	@ApiModelProperty(value = "기준가", example = "1000")
	private String priceStd;

	@ApiModelProperty(value = "투자 성향", example = "0.75")
	private Double tendency;
}
