package org.scoula.View.Event.dto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "퀴즈 응답 DTO", description = "퀴즈 응답 DTO")
public class QuizDto {
	private String id;
	private String question;
	private String choices; // JSON 배열을 List로 매핑
	private int answer;           // 0-based index
	private String explanation;

}
