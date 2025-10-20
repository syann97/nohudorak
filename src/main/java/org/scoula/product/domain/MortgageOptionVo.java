package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 모기지 옵션 정보를 담는 VO 클래스
 */
@ApiModel(value = "MortgageOptionVo", description = "모기지(주택담보) 상품 옵션 정보")
@Data
public class MortgageOptionVo {

	@ApiModelProperty(value = "옵션 코드 (ID 역할)", example = "101")
	private Integer optionCd;

	@ApiModelProperty(value = "담보 유형명", example = "주택담보")
	private String mrtgTypeNm;

	@ApiModelProperty(value = "대출 상환 유형명", example = "원리금균등")
	private String rpayTypeNm;

	@ApiModelProperty(value = "대출 금리 유형명", example = "고정금리")
	private String lendRateTypeNm;

	@ApiModelProperty(value = "대출 금리 최저", example = "3.5")
	private Double lendRateMin;

	@ApiModelProperty(value = "대출 금리 최고", example = "5.0")
	private Double lendRateMax;

	@ApiModelProperty(value = "전월 취급 평균 금리", example = "4.2")
	private Double lendRateAvg;
}
