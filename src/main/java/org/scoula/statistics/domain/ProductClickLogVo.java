package org.scoula.statistics.domain;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 클릭 로그 VO(Value Object) 클래스
 * DB의 product_click_log 테이블과 매핑
 */
@ApiModel(value = "클릭 로그 VO", description = "DB product_click_log 테이블과 매핑되는 VO 객체")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductClickLogVo {

	@ApiModelProperty(value = "기본 키 ID", example = "1")
	private Long id;

	@ApiModelProperty(value = "금융상품 코드", example = "DEP001")
	private String finPrdtCd;

	@ApiModelProperty(value = "사용자 이메일", example = "user@example.com")
	private String email;

	@ApiModelProperty(value = "클릭을 유발한 주체(사용자, 시스템 등)", example = "USER")
	private String triggeredBy;

	@ApiModelProperty(value = "로그 생성 시각", example = "2025-08-20T12:00:00")
	private LocalDateTime createdAt;
}
