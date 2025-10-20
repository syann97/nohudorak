package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * Deposit(예금) 옵션 DTO
 */
@ApiModel(value = "DepositOptionDto", description = "예금 상품 옵션 정보")
@SuperBuilder
@Getter
public class DepositOptionDto extends DepositSavingOptionDto {

	@ApiModelProperty(value = "저축금리 유형명", example = "단리, 복리 등")
	private String intrRateTypeNm;
}
