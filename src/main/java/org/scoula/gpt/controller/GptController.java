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
import org.springframework.web.context.request.async.DeferredResult; // --- 1. DeferredResult 임포트

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; // --- 2. 로깅을 위한 임포트

/**
 * GPT 관련 API 요청을 처리하는 컨트롤러입니다.
 * (비동기 응답 처리를 위해 DeferredResult 사용)
 */
@Api(tags = "GPT 채팅 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/gpt")
@Log4j2 // --- 3. 로깅 어노테이션 추가
public class GptController {

	private final GptService gptService;

	// --- 4. 타임아웃 시간 (예: 60초) ---
	private static final long ASYNC_TIMEOUT_MS = 60_000L;

	@ApiOperation(value = "GPT 채팅 요청 (비동기)", notes = "사용자의 질문을 비동기적으로 처리하여 AI가 답변을 반환합니다.") // --- 5. @ApiOperation 설명 수정
	@ApiResponses({
		@ApiResponse(code = 200, message = "성공적으로 AI 답변 반환", response = ChatResponseDto.class),
		@ApiResponse(code = 400, message = "잘못된 요청 형식", response = ErrorResponse.class),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@PostMapping("/chat")
	public DeferredResult<ChatResponseDto> chat( // --- 6. 반환 타입을 DeferredResult<ChatResponseDto>로 변경
		@ApiParam(value = "사용자의 질문 정보", required = true) @RequestBody ChatRequestDto chatRequestDto) {

		log.info("비동기 챗 요청 수신: {}", chatRequestDto.getQuestion());

		// 7. DeferredResult 객체 생성 (타임아웃 설정)
		DeferredResult<ChatResponseDto> deferredResult = new DeferredResult<>(ASYNC_TIMEOUT_MS);

		// 8. 타임아웃 발생 시 처리 로직
		deferredResult.onTimeout(() -> {
			log.warn("GPT 챗 요청 시간 초과 (Timeout)");
			// ErrorResponse는 예시이며, 실제 예외 처리 DTO나 Exception 객체를 사용해야 합니다.
			// @ExceptionHandler가 처리할 수 있도록 Throwable을 전달하는 것이 좋습니다.
			deferredResult.setErrorResult(
				new RuntimeException("Request timed out after " + (ASYNC_TIMEOUT_MS / 1000) + " seconds.")
			);
		});

		// 9. 비동기 서비스 호출
		gptService.getChatResponse(chatRequestDto)
			.whenComplete((response, throwable) -> {
				// 10. 비동기 작업 완료 시 콜백
				if (throwable != null) {
					// 10-1. 예외 발생 시
					log.error("비동기 GPT 챗 처리 중 예외 발생", throwable);
					deferredResult.setErrorResult(throwable); // 예외를 Spring MVC에 전파
				} else {
					// 10-2. 성공 시
					log.info("비동기 챗 응답 성공");
					deferredResult.setResult(response); // 성공 응답 설정
				}
			});

		// 11. DeferredResult를 즉시 반환 (서블릿 스레드는 여기서 해제됨)
		log.info("DeferredResult 반환, 서블릿 스레드 해제.");
		return deferredResult;
	}
}