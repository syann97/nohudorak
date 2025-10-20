package org.scoula.statistics.domain;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클릭 로그 전달 히스토리 VO(Value Object) 클래스
 * DB의 click_stats_send_history 테이블과 매핑
 */
@ApiModel(value = "클릭 로그 전달 히스토리 VO", description = "DB click_stats_send_history 테이블과 매핑되는 VO 객체")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClickStatsSendHistoryVo {

	@ApiModelProperty(value = "기본 키 ID", example = "1")
	private Long id;

	@ApiModelProperty(value = "로그 전송 시각", example = "2025-08-20T12:00:00")
	private LocalDateTime sentAt;

	@ApiModelProperty(value = "레코드 생성 시각", example = "2025-08-20T11:50:00")
	private LocalDateTime createdAt;
}
