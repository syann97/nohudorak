package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

/**
 * Mortgage(주택담보대출) 옵션 DTO
 */
@ApiModel(value = "MortgageOptionDto", description = "주택담보대출 상품 옵션 정보를 담는 DTO")
@Builder
@Getter
public class MortgageOptionDto {

	@ApiModelProperty(value = "담보유형")
	private String mrtgTypeNm;

	@ApiModelProperty(value = "대출상환유형")
	private String rpayTypeNm;

	@ApiModelProperty(value = "대출금리유형")
	private String lendRateTypeNm;

	@ApiModelProperty(value = "대출금리 최저")
	private Double lendRateMin;

	@ApiModelProperty(value = "대출금리 최고")
	private Double lendRateMax;

	@ApiModelProperty(value = "전월 취급 평균금리")
	private Double lendRateAvg;
}
