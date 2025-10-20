package org.scoula.gift.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 수증자별 세금 상세 정보 DTO
 */
@ApiModel(value = "수증자별 세금 상세 정보 DTO", description = "수증자 이름, 총 증여액, 예상 증여세 정보를 담습니다.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientTaxDetailDto {

	@ApiModelProperty(value = "수증자 이름", example = "홍길동", required = true)
	private String recipientName;

	@ApiModelProperty(value = "해당 수증자가 받은 총 증여액", example = "50000000", required = true)
	private long totalGiftAmount;

	@ApiModelProperty(value = "해당 수증자의 예상 증여세", example = "1000000", required = true)
	private long estimatedTax;
}
