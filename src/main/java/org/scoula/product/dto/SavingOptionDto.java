package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Saving(적금) 옵션 DTO
 */
@ApiModel(value = "SavingOptionDto", description = "적금 상품 옵션 정보 DTO")
@SuperBuilder
@Getter
public class SavingOptionDto extends DepositSavingOptionDto {

	@ApiModelProperty(value = "적립 유형명")
	private String rsrvTypeNm;

	@ApiModelProperty(value = "저축금리 유형명")
	private String intrRateTypeNm;
}
