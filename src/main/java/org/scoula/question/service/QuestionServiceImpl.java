package org.scoula.question.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;
import org.scoula.gpt.service.GptService;
import org.scoula.question.dto.QuestionResponseDto;
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

	@Override
	public QuestionResponseDto handleTextQuestion(String text, Authentication authentication) {
		String userEmail = authentication != null ? authentication.getName() : "비로그인";
		log.info("텍스트 질문 처리 시작 - 사용자: {}, 질문: '{}'", userEmail, text);

		ChatResponseDto gptResponseDto = gptService.getChatResponse(new ChatRequestDto(text));
		log.info("GPT 응답 = {}", gptResponseDto.getAnswer());

		return new QuestionResponseDto("SUCCESS", "텍스트 질문이 성공적으로 처리되었습니다.", text, gptResponseDto.getAnswer());
	}

	@Override
	public QuestionResponseDto handleVoiceQuestion(MultipartFile audioFile, Authentication authentication) throws
		IOException,
		InterruptedException {
		String userEmail = authentication != null ? authentication.getName() : "비로그인";
		log.info("음성 질문 처리 시작 - 사용자: {}, 파일: {}", userEmail, audioFile.getOriginalFilename());

		// 1. 음성 파일을 텍스트로 변환 (FFmpeg + CLOVA API)
		String speechToText = convertSpeechToText(audioFile);
		log.info("음성 -> 텍스트 변환 결과: '{}'", speechToText);

		// 2. 변환된 텍스트로 GPT 서비스 호출
		ChatResponseDto gptResponseDto = gptService.getChatResponse(new ChatRequestDto(speechToText));
		log.info("GPT 응답 = {}", gptResponseDto.getAnswer());

		return new QuestionResponseDto("SUCCESS", "음성 질문이 성공적으로 처리되었습니다.", speechToText, gptResponseDto.getAnswer());
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
