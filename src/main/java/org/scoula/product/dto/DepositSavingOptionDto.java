package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Deposit(예금)과 Saving(적금)에 공통으로 필요한 옵션 필드 DTO
 */
@ApiModel(value = "DepositSavingOptionDto", description = "예금 및 적금 공통 옵션 정보")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class DepositSavingOptionDto {

	@ApiModelProperty(value = "저축 기간[개월]", example = "12")
	private String saveTrm;

	@ApiModelProperty(value = "저축금리", example = "3.5%")
	private String intrRate;

	@ApiModelProperty(value = "최고 우대금리", example = "4.0%")
	private String intrRate2;
}
