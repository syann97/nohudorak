package org.scoula.question.service;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import org.scoula.question.dto.QuestionResponseDto;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

/**
 * 질문 처리 관련 비즈니스 로직을 정의하는 서비스 인터페이스
 */
public interface QuestionService {

	/**
	 * 텍스트 질문을 처리하고 GPT 답변을 반환합니다.
	 *
	 * @param text 텍스트 질문
	 * @param authentication 사용자 인증 정보
	 * @return 처리 결과 DTO
	 */
	CompletableFuture<QuestionResponseDto> handleTextQuestion(String text, Authentication authentication);

	/**
	 * 음성 파일을 텍스트로 변환하고 GPT 답변을 반환합니다.
	 *
	 * @param audioFile 음성 파일
	 * @param authentication 사용자 인증 정보
	 * @return 처리 결과 DTO
	 * @throws IOException 파일 처리 또는 API 호출 중 발생할 수 있는 예외
	 * @throws InterruptedException FFmpeg 프로세스 중 발생할 수 있는 예외
	 */
	CompletableFuture<QuestionResponseDto> handleVoiceQuestion(MultipartFile audioFile, Authentication authentication);
}
