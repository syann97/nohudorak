package org.scoula.question.controller;

import org.scoula.question.dto.QuestionResponseDto;
import org.scoula.question.service.QuestionService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
@Api(tags = "질문 API", description = "텍스트와 음성 질문 처리 API")
@Log4j2
public class QuestionController {

	private final QuestionService questionService;
	private static final long ASYNC_TIMEOUT_MS = 60_000L; // 60초

	@PostMapping("/text")
	public DeferredResult<QuestionResponseDto> handleTextQuestion(
		@RequestBody String textQuestion, // DTO로 받는 것을 권장
		Authentication authentication) {

		DeferredResult<QuestionResponseDto> deferredResult = new DeferredResult<>(ASYNC_TIMEOUT_MS);

		questionService.handleTextQuestion(textQuestion, authentication)
			.whenComplete((response, throwable) -> {
				if (throwable != null) {
					deferredResult.setErrorResult(throwable);
				} else {
					deferredResult.setResult(response);
				}
			});

		return deferredResult;
	}

	@PostMapping("/voice")
	public DeferredResult<QuestionResponseDto> handleVoiceQuestion(
		@RequestParam("audio") MultipartFile audioFile,
		Authentication authentication) {

		DeferredResult<QuestionResponseDto> deferredResult = new DeferredResult<>(ASYNC_TIMEOUT_MS);

		questionService.handleVoiceQuestion(audioFile, authentication)
			.whenComplete((response, throwable) -> {
				if (throwable != null) {
					log.error("음성 질문 처리 컨트롤러 예외", throwable);
					deferredResult.setErrorResult(throwable);
				} else {
					deferredResult.setResult(response);
				}
			});

		return deferredResult;
	}
}
