package org.scoula.gift.dto;

import java.util.Date;

import org.scoula.gift.domain.RecipientVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "수증자 생성/수정 요청 DTO", description = "수증자 정보 생성 및 수정을 위해 클라이언트에서 받는 데이터")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientRequestDto {

	@ApiModelProperty(value = "관계", example = "자녀", required = true)
	private String relationship;

	@ApiModelProperty(value = "수증자 이름", example = "홍길동", required = true)
	private String recipientName;

	@ApiModelProperty(value = "생년월일", example = "2000-01-01", required = true)
	private Date birthDate;

	@ApiModelProperty(value = "결혼 여부", example = "false", required = true)
	private Boolean isMarried;

	@ApiModelProperty(value = "사전 증여 여부", example = "true", required = true)
	private Boolean hasPriorGift;

	@ApiModelProperty(value = "사전 증여 가액", example = "10000000")
	private Long priorGiftAmount;

	@ApiModelProperty(value = "증여세 납부자 (증여자/수증자)", example = "수증자", required = true)
	private String giftTaxPayer;

	public RecipientVo toVo(String email) {
		return new RecipientVo(
			null,
			email,
			this.relationship,
			this.recipientName,
			this.birthDate,
			this.isMarried,
			this.hasPriorGift,
			this.priorGiftAmount,
			this.giftTaxPayer
		);
	}
}
