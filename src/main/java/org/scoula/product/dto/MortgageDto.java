package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * Mortgage(주택담보대출) DTO
 */
@ApiModel(value = "MortgageDto", description = "주택담보대출 상품 상세 정보를 담는 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
public class MortgageDto extends ProductDetailDto<MortgageOptionDto> {

	@ApiModelProperty(value = "대출 부대비용")
	private String loanInciExpn;

	@ApiModelProperty(value = "중도상환 수수료")
	private String erlyRpayFee;

	@ApiModelProperty(value = "연체 이자율")
	private String dlyRate;

	@ApiModelProperty(value = "대출 한도")
	private String loanLmt;
}
