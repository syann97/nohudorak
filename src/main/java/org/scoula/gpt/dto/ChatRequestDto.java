package org.scoula.gpt.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 클라이언트에서 서버로 보내는 요청을 담는 DTO 입니다.
 * 사용자의 질문(question)을 필드로 가집니다.
 */
@ApiModel(value = "GPT 채팅 요청 DTO", description = "사용자의 질문을 서버로 전달하기 위한 데이터 모델")
@Data
@AllArgsConstructor
public class ChatRequestDto {

	@ApiModelProperty(value = "사용자의 질문 내용", example = "오늘 날씨 알려줘", required = true)
	private String question;
}
