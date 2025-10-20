package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Fund(펀드) 간단 옵션 DTO
 * 펀드 상품 전체 목록 조회 시 필요한 필드만 사용
 */
@ApiModel(value = "FundSimpleOptionDto", description = "펀드 간단 옵션 정보를 담는 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class FundSimpleOptionDto {

	@ApiModelProperty(value = "3개월 수익률")
	private String rate3mon;

	@ApiModelProperty(value = "위험등급")
	private String riskGrade;

	@ApiModelProperty(value = "기준가")
	private String priceStd;
}
