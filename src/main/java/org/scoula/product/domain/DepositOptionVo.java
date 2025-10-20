package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 예금 상품의 옵션 정보를 담는 VO(Value Object) 클래스
 */
@ApiModel(value = "DepositOptionVo", description = "예금 상품 옵션 정보")
@Data
public class DepositOptionVo {

	@ApiModelProperty(value = "옵션 코드 (ID 역할)", example = "101")
	private Integer optionCd;

	@ApiModelProperty(value = "저축 기간 [개월]", example = "12")
	private String saveTrm;

	@ApiModelProperty(value = "저축 금리 유형명", example = "단리")
	private String intrRateTypeNm;

	@ApiModelProperty(value = "기본 금리", example = "1.5")
	private String intrRate;

	@ApiModelProperty(value = "최고 우대 금리", example = "2.0")
	private String intrRate2;
}
