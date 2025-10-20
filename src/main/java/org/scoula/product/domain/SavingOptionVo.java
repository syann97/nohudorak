package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 적금 옵션 정보 DTO
 */
@ApiModel(value = "SavingOptionVo", description = "적금 옵션 상세 정보")
@Data
public class SavingOptionVo {

	@ApiModelProperty(value = "옵션 코드 (ID 역할)", example = "101")
	private Integer optionCd;

	@ApiModelProperty(value = "저축 기간 [개월]", example = "12")
	private String saveTrm;

	@ApiModelProperty(value = "적립 유형명", example = "자유적립식")
	private String rsrvTypeNm;

	@ApiModelProperty(value = "저축금리 유형명", example = "단리")
	private String intrRateTypeNm;

	@ApiModelProperty(value = "저축 금리", example = "2.5")
	private String intrRate;

	@ApiModelProperty(value = "최고 우대 금리", example = "3.0")
	private String intrRate2;
}
