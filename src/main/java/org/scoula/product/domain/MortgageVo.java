package org.scoula.product.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * vo.Mortgage(주택담보대출)
 */
@ApiModel(value = "MortgageVo", description = "주택담보대출 상품 정보")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class MortgageVo extends ProductVo<MortgageOptionVo> {

	@ApiModelProperty(value = "대출 부대비용", example = "100,000원")
	private String loanInciExpn;

	@ApiModelProperty(value = "중도상환 수수료", example = "0.5%")
	private String erlyRpayFee;

	@ApiModelProperty(value = "연체 이자율", example = "5%")
	private String dlyRate;

	@ApiModelProperty(value = "대출 한도", example = "500,000,000원")
	private String loanLmt;
}
