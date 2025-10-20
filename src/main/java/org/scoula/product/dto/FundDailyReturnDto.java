package org.scoula.product.dto;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

import org.scoula.product.domain.FundDailyReturnVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 펀드 일별 수익률 DTO
 */
@ApiModel(value = "FundDailyReturnDto", description = "펀드의 일별 수익률 정보를 담는 DTO")
@SuperBuilder
@ToString(callSuper = true)
@Getter
@AllArgsConstructor
public class FundDailyReturnDto {

	@ApiModelProperty(value = "기록 날짜 (yyyy-MM-dd 형식)", example = "2025-08-20")
	private String recordDate;

	@ApiModelProperty(value = "수익률", example = "0.025")
	private BigDecimal returnRate;

	/**
	 * VO -> DTO 변환 메서드
	 */
	public static FundDailyReturnDto of(FundDailyReturnVo vo) {
		return new FundDailyReturnDto(
			vo.getRecordDate().format(DateTimeFormatter.ISO_DATE),
			vo.getReturnRate()
		);
	}
}
