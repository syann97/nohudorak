package org.scoula.product.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 펀드 일별 수익률 정보를 담는 VO 클래스
 */
@ApiModel(value = "FundDailyReturnVo", description = "펀드 일별 수익률 정보")
@Data
public class FundDailyReturnVo {

	@ApiModelProperty(value = "식별 ID", example = "1")
	private Long id;

	@ApiModelProperty(value = "펀드 코드", example = "FUND12345")
	private String fundCode;

	@ApiModelProperty(value = "기록일자", example = "2025-08-20")
	private LocalDate recordDate;

	@ApiModelProperty(value = "수익률", example = "0.015")
	private BigDecimal returnRate;

	@ApiModelProperty(value = "데이터 생성 시각", example = "2025-08-20T14:30:00")
	private LocalDateTime createdAt;

	@ApiModelProperty(value = "데이터 수정 시각", example = "2025-08-20T15:00:00")
	private LocalDateTime updatedAt;
}
