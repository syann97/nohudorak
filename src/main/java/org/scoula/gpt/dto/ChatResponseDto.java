package org.scoula.gpt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 서버가 AI의 답변을 담아 클라이언트로 보내는 응답 DTO 입니다.
 * AI의 답변(answer)을 필드로 가집니다.
 */
@ApiModel(value = "GPT 채팅 응답 DTO", description = "서버가 생성한 AI 답변을 담는 데이터 모델")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponseDto {

	@ApiModelProperty(value = "AI가 생성한 답변 내용", example = "오늘 서울 날씨는 맑고 최고 기온은 25도입니다.", required = true)
	private String answer;
}
