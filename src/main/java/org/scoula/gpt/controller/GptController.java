package org.scoula.gpt.controller;

import org.scoula.exception.ErrorResponse;
import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;
import org.scoula.gpt.service.GptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * GPT 관련 API 요청을 처리하는 컨트롤러입니다.
 */
@Api(tags = "GPT 채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
public class GptController {

	private final GptService gptService;

	@ApiOperation(value = "GPT 채팅 요청", notes = "사용자의 질문을 받아 AI가 답변을 반환합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공적으로 AI 답변 반환", response = ChatResponseDto.class),
		@ApiResponse(code = 400, message = "잘못된 요청 형식", response = ErrorResponse.class),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@PostMapping("/chat")
	public ResponseEntity<ChatResponseDto> chat(
		@ApiParam(value = "사용자의 질문 정보", required = true) @RequestBody ChatRequestDto chatRequestDto) {

		ChatResponseDto chatResponseDto = gptService.getChatResponse(chatRequestDto);
		return ResponseEntity.ok(chatResponseDto);
	}
}
