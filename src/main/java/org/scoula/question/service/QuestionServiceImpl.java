package org.scoula.question.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;
import org.scoula.gpt.service.GptService;
import org.scoula.question.dto.QuestionResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Qualifier;

import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.builder.FFmpegBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class QuestionServiceImpl implements QuestionService {

	private final GptService gptService;

	@Value("${naver.clova.api.key.secret}")
	private String clovaSpeechApiKey;

	// --- 1. 비동기 I/O 작업을 위한 Executor 주입 ---
	@Autowired
	@Qualifier("ragTaskExecutor")
	private Executor taskExecutor;

	@Override
	public CompletableFuture<QuestionResponseDto> handleTextQuestion(String text, Authentication authentication) { // --- 2. 반환 타입 변경 ---
		String userEmail = authentication != null ? authentication.getName() : "비로그인";
		log.info("비동기 텍스트 질문 처리 시작 - 사용자: {}, 질문: '{}'", userEmail, text);

		// 3. gptService의 비동기 메서드 호출 후, thenApply로 결과 변환
		return gptService.getChatResponse(new ChatRequestDto(text))
			.thenApply(gptResponseDto -> {
				// 이 블록은 GPT 응답이 오면 실행됨
				log.info("GPT 응답 = {}", gptResponseDto.getAnswer());
				return new QuestionResponseDto("SUCCESS", "텍스트 질문이 성공적으로 처리되었습니다.", text, gptResponseDto.getAnswer());
			})
			.exceptionally(ex -> {
				// gptService.getChatResponse에서 예외 발생 시
				log.error("텍스트 질문 처리 중 오류", ex);
				return new QuestionResponseDto("ERROR", ex.getMessage(), text, null);
			});
	}

	@Override
	public CompletableFuture<QuestionResponseDto> handleVoiceQuestion(MultipartFile audioFile, Authentication authentication) { // --- 4. 반환 타입 변경 및 throws 제거 ---
		String userEmail = authentication != null ? authentication.getName() : "비로그인";
		log.info("비동기 음성 질문 처리 시작 - 사용자: {}, 파일: {}", userEmail, audioFile.getOriginalFilename());

		// 5. STT (I/O 작업)를 별도 스레드에서 비동기 실행
		CompletableFuture<String> speechToTextFuture = CompletableFuture.supplyAsync(() -> {
			try {
				log.info("비동기: 음성 -> 텍스트 변환 시작 (Thread: {})", Thread.currentThread().getName());
				// convertSpeechToText는 I/O가 발생하므로 비동기 처리
				return convertSpeechToText(audioFile);
			} catch (IOException | InterruptedException e) {
				// 6. Checked Exception을 Unchecked로 변환하여 비동기 파이프라인에 전파
				log.error("음성 변환(STT) 작업 중 오류", e);
				throw new RuntimeException("음성 파일 변환(STT)에 실패했습니다.", e);
			}
		}, taskExecutor); // 정의된 스레드 풀 사용

		// 7. STT 작업이 완료되면(thenCompose), 비동기 GPT 서비스 호출
		return speechToTextFuture.thenCompose(speechToText -> {
			log.info("음성 -> 텍스트 변환 결과: '{}'", speechToText);

			// 8. gptService.getChatResponse는 이미 CompletableFuture를 반환하므로 thenCompose 사용
			CompletableFuture<ChatResponseDto> gptFuture = gptService.getChatResponse(new ChatRequestDto(speechToText));

			// 9. GPT 응답이 오면(thenApply) 최종 DTO로 변환
			return gptFuture.thenApply(gptResponseDto -> {
				log.info("GPT 응답 = {}", gptResponseDto.getAnswer());
				return new QuestionResponseDto("SUCCESS", "음성 질문이 성공적으로 처리되었습니다.", speechToText, gptResponseDto.getAnswer());
			});

		}).exceptionally(ex -> {
			// 10. STT 또는 GPT 작업 중 발생한 모든 예외 처리
			log.error("음성 질문 처리 파이프라인 중 오류", ex);
			// 실패 시 사용자에게 보여줄 DTO 반환
			String originalText = (ex.getCause() != null) ? null : ex.getMessage(); // STT 실패 시 text는 null
			return new QuestionResponseDto("ERROR", "질문 처리 중 오류가 발생했습니다: " + ex.getMessage(), originalText, null);
		});
	}

	/**
	 * 음성 파일을 텍스트로 변환 (FFmpeg + CLOVA Speech API)
	 * @param webmAudioFile 변환할 음성 파일
	 * @return 변환된 텍스트
	 */
	private String convertSpeechToText(MultipartFile webmAudioFile) throws IOException, InterruptedException {
		File inputWebmFile = null;
		File outputWavFile = null;
		String tempDir = System.getProperty("java.io.tmpdir");

		try {
			// 1. 전송받은 WebM 파일을 서버에 임시 저장
			String originalFilename = "input_" + System.currentTimeMillis() + ".webm";
			inputWebmFile = new File(tempDir, originalFilename);
			webmAudioFile.transferTo(inputWebmFile);

			// 2. FFmpeg으로 WebM -> WAV 변환
			FFmpeg ffmpeg = new FFmpeg(); // (실제 프로덕션에서는 FFmpeg 경로를 명시하는 것이 좋습니다)
			String wavFilename = originalFilename.replace(".webm", ".wav");
			outputWavFile = new File(tempDir, wavFilename);

			FFmpegBuilder builder = new FFmpegBuilder()
				.setInput(inputWebmFile.getAbsolutePath())
				.overrideOutputFiles(true)
				.addOutput(outputWavFile.getAbsolutePath())
				.setAudioCodec("pcm_s16le")
				.setAudioSampleRate(16_000)
				.setAudioChannels(1)
				.done();
			ffmpeg.run(builder);
			log.info("FFmpeg 변환 성공: {} -> {}", inputWebmFile.getName(), outputWavFile.getName());

			// 3. 변환된 WAV 파일을 CLOVA Speech API로 전송
			return callClovaApi(outputWavFile);

		} finally {
			// 4. 작업 후 임시 파일들 삭제
			if (inputWebmFile != null) {
				Files.deleteIfExists(inputWebmFile.toPath());
			}
			if (outputWavFile != null) {
				Files.deleteIfExists(outputWavFile.toPath());
			}
		}
	}

	/**
	 * CLOVA Speech API 호출
	 * @param wavFile API로 전송할 WAV 파일
	 * @return API가 반환한 텍스트
	 */
	private String callClovaApi(File wavFile) throws IOException {
		log.info("Clova API 호출 시작 : {}", wavFile.getName());
		String apiUrl = "https://clovaspeech-gw.ncloud.com/recog/v1/stt?lang=Kor";
		RestTemplate restTemplate = new RestTemplate();

		HttpHeaders headers = new HttpHeaders();
		headers.set("X-CLOVASPEECH-API-KEY", clovaSpeechApiKey);
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

		byte[] fileContent = Files.readAllBytes(wavFile.toPath());
		HttpEntity<byte[]> requestEntity = new HttpEntity<>(fileContent, headers);

		log.info("CLOVA Speech API 호출 시작...");
		ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, requestEntity, Map.class);
		log.info("CLOVA Speech API 응답 수신. 상태 코드: {}", response.getStatusCode());

		if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
			String resultText = (String)response.getBody().get("text");
			if (resultText == null || resultText.trim().isEmpty()) {
				log.warn("CLOVA API가 음성을 인식하지 못했거나 빈 텍스트를 반환했습니다.");
				// 비어있지만 오류는 아닌 경우, 빈 문자열 반환
				return "";
			}
			return resultText;
		} else {
			log.error("CLOVA API 호출 실패. 상태코드: {}, 응답: {}", response.getStatusCode(), response.getBody());
			throw new IOException("CLOVA API 호출 실패: " + response.getStatusCode());
		}
	}
}
