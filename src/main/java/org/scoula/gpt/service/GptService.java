package org.scoula.gpt.service;

import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;

public interface GptService {
	/**
	 * 채팅 요청을 받아 AI의 응답을 반환합니다.
	 * @param chatRequest 사용자의 질문이 담긴 요청 객체
	 * @return AI의 답변이 담긴 응답 객체
	 */
	ChatResponseDto getChatResponse(ChatRequestDto chatRequest);
}
