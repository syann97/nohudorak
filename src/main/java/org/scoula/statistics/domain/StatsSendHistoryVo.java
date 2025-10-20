package org.scoula.statistics.domain;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 집계 데이터 전송 기록 VO(Value Object) 클래스
 * DB의 stats_send_history 테이블과 매핑
 */
@ApiModel(value = "집계 데이터 전송 기록 VO", description = "DB stats_send_history 테이블과 매핑되는 VO 객체")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsSendHistoryVo {

	@ApiModelProperty(value = "기본 키 ID", example = "1")
	private Long id;

	@ApiModelProperty(value = "통계 유형", example = "PRODUCT_CLICK")
	private String statType;

	@ApiModelProperty(value = "전송 시각", example = "2025-08-20T12:00:00")
	private LocalDateTime sentAt;
}
