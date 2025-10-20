package org.scoula.View.preference.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(value = "사용자 성향 설문 요청 DTO", description = "사용자가 제출한 5개 질문의 답변을 담는 객체")
public class PreferenceRequestDto {
	@ApiModelProperty(value = "질문 1의 답변 (1~4)", required = true, example = "1")
	private int q1;

	@ApiModelProperty(value = "질문 2의 답변 (1~3)", required = true, example = "2")
	private int q2;

	@ApiModelProperty(value = "질문 3의 답변 (1~3)", required = true, example = "3")
	private int q3;

	@ApiModelProperty(value = "질문 4의 답변 (1~4)", required = true, example = "4")
	private int q4;

	@ApiModelProperty(value = "질문 5의 답변 (1~3)", required = true, example = "1")
	private int q5;
}