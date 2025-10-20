package org.scoula.question.controller;

import java.util.Map;

import org.scoula.question.dto.QuestionResponseDto;
import org.scoula.question.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/question")
@Api(tags = "질문 API", description = "텍스트와 음성 질문 처리 API")
@Log4j2
public class QuestionController {

	// 이제 GptService가 아닌 QuestionService에 의존합니다.
	private final QuestionService questionService;

	@ApiOperation(value = "텍스트 질문 처리", notes = "텍스트 질문만 받아서 처리합니다.")
	@PostMapping("/text")
	public ResponseEntity<QuestionResponseDto> handleTextQuestion(
		@RequestBody Map<String, String> request,
		Authentication authentication) {

		String text = request.get("text");
		if (text == null || text.trim().isEmpty()) {
			QuestionResponseDto errorResponse = new QuestionResponseDto("ERROR", "텍스트 질문이 필요합니다.");
			return ResponseEntity.badRequest().body(errorResponse);
		}

		// 서비스 계층에 로직 위임
		QuestionResponseDto response = questionService.handleTextQuestion(text.trim(), authentication);
		return ResponseEntity.ok(response);
	}

	@ApiOperation(value = "음성 질문 처리", notes = "음성 파일만 받아서 처리하고 GPT를 통해 답변을 반환합니다.")
	@PostMapping("/voice")
	public ResponseEntity<QuestionResponseDto> handleVoiceQuestion(
		@RequestParam(value = "audio") MultipartFile audioFile,
		Authentication authentication) {

		if (audioFile == null || audioFile.isEmpty()) {
			QuestionResponseDto errorResponse = new QuestionResponseDto("ERROR", "음성 파일이 필요합니다.");
			return ResponseEntity.badRequest().body(errorResponse);
		}

		try {
			// 서비스 계층에 로직 위임
			QuestionResponseDto response = questionService.handleVoiceQuestion(audioFile, authentication);
			return ResponseEntity.ok(response);

		} catch (Exception e) {
			log.error("음성 질문 처리 중 컨트롤러에서 오류 발생: {}", e.getMessage(), e);
			QuestionResponseDto errorResponse = new QuestionResponseDto("ERROR",
				"음성 질문 처리 중 오류가 발생했습니다: " + e.getMessage());
			return ResponseEntity.internalServerError().body(errorResponse);
		}
	}
}
