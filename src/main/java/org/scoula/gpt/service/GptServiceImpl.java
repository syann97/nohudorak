package org.scoula.gpt.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.concurrent.Executor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Qualifier;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.embeddings.CreateEmbeddingResponse;
import com.openai.models.embeddings.EmbeddingCreateParams;

import lombok.extern.log4j.Log4j2;

/**
 * GptService 인터페이스의 최종 구현체입니다.
 * RAG(Retrieval-Augmented Generation) 로직이 적용되었습니다.
 */

@Service
@Log4j2
public class GptServiceImpl implements GptService, ApplicationListener<ContextRefreshedEvent>, ApplicationContextAware {
	private static final String EMBEDDING_MODEL = "text-embedding-3-small";
	private static final int TOP_K_CHUNKS = 3; // 검색할 관련 정보 조각의 수
	private final ResourceLoader resourceLoader;
	private final AtomicBoolean isReady = new AtomicBoolean(false);

	// === 의존성 및 상태 필드 ===
	private final OpenAIClient openAiClient;
	private final Map<String, List<Float>> vectorStore = new ConcurrentHashMap<>();

	@Value("${rag.pdf.path}")
	private String pdfPath;

	@Value("${gpt.system.prompt}")
	private String systemPrompt;

	@Autowired
	@Qualifier("ragTaskExecutor") // Step 1에서 정의한 Executor 주입
	private Executor taskExecutor;

	private ApplicationContext applicationContext;

	/**
	 * [신규] ApplicationContextAware 인터페이스의 구현 메서드
	 * Spring이 이 빈을 생성할 때 ApplicationContext를 주입해줍니다.
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}


	/**
	 * 생성자: API 키를 주입받아 OpenAIClient를 초기화합니다.
	 */
	public GptServiceImpl(ResourceLoader resourceLoader, @Value("${openai.api.key}") String apiKey) {
		this.resourceLoader = resourceLoader;
		this.openAiClient = OpenAIOkHttpClient.builder()
			.apiKey(apiKey)
			.build();
		log.info("GptServiceImpl 초기화 완료: OpenAIClient 생성");
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event) {
		// [수정] 이벤트의 컨텍스트를 가져옵니다.
		ApplicationContext context = event.getApplicationContext();

		// [수정] 이 컨텍스트의 부모(parent)가 null인지 확인합니다.
		// 오직 Root Context만이 부모가 null입니다.
		if (context.getParent() == null) {
			log.info("=========================================");
			log.info("Spring 'Root Context' 준비 완료. RAG 비동기 초기화를 '시작'합니다.");
			log.info("=========================================");

			// @Async가 붙은 별도의 메서드를 호출
			GptServiceImpl selfProxy = applicationContext.getBean(GptServiceImpl.class);
			selfProxy.startRagInitialization();
		} else {
			log.info("Spring 'Servlet Context' 준비 완료. (RAG 초기화는 Root Context에서 이미 시작됨)");
		}
	}

	/**
	 * [수정] 초기화 메서드: @PostConstruct 제거, @Async만 유지
	 * 이제 이 메서드는 Spring 시작 시점이 아닌, ContextRefreshedEvent 이후에 호출됩니다.
	 */
	@Async("ragTaskExecutor")
	public void startRagInitialization() { // <-- 메서드 이름 변경 (public 유지)
		try {
			log.info("RAG 데이터 비동기 로딩 시작... (Thread: {})", Thread.currentThread().getName());

			initializeRagData(this.pdfPath); // 실제 로직 호출

			this.isReady.set(true); // 성공 시 플래그 설정
			log.info("=========================================");
			log.info("RAG 서비스 준비 완료 (isReady = true)");
			log.info("=========================================");

		} catch (Exception e) {
			log.error("!!!!!! RAG 비동기 초기화 중 치명적 오류 발생 !!!!!!", e);
			e.printStackTrace();
		}
	}


	/**
	 * RAG 데이터 준비 단계: PDF를 읽어 벡터 저장소를 구축합니다.
	 * [개선점] TaskRejectedException 해결을 위해 '배치(Batch)' 단위로 작업을 분할하여 처리합니다.
	 *
	 * @param filePath PDF 파일 경로
	 * @throws IOException 파일 읽기 실패 시
	 */
	private void initializeRagData(String filePath) throws IOException {
		log.info("RAG 데이터 초기화를 (비동기) 시작합니다. 경로: {}", filePath);
		Resource ragResource = resourceLoader.getResource(filePath);

		if (!ragResource.exists()) {
			log.error("지정된 경로에 PDF 파일이 없습니다: {}", filePath);
			throw new FileNotFoundException("PDF 파일을 찾을 수 없습니다.");
		}

		String rawText;
		try (PDDocument document = PDDocument.load(ragResource.getInputStream())) {
			PDFTextStripper pdfStripper = new PDFTextStripper();
			rawText = pdfStripper.getText(document);
		}

		// --- 텍스트 정제 로직 (기존과 동일) ---
		String contentText = rawText;
		int tocEndIndex = rawText.lastIndexOf("찾아보기"); // 목차의 마지막 '찾아보기' 제목
		int contentStartIndex = -1;

		if (tocEndIndex != -1) {
			Pattern chapterStartPattern = Pattern.compile("(?m)^\\s*ㄱ\\s*$");
			Matcher matcher = chapterStartPattern.matcher(rawText.substring(tocEndIndex));
			if (matcher.find()) {
				contentStartIndex = tocEndIndex + matcher.start();
			}
		}

		if (contentStartIndex != -1) {
			contentText = rawText.substring(contentStartIndex);
			log.info("문서 구조 분석을 통해 본문 시작점을 찾았습니다.");
		} else {
			log.warn("PDF에서 본문 시작점('ㄱ' 섹션)을 찾지 못했습니다. 목차 등이 포함될 수 있습니다.");
		}

		String cleanedText = contentText
			.replaceAll("(?m)^경제금융용어 700선.*$", "")
			.replaceAll("(?m)^[ivxlcdm\\d]+\\s*$", "")
			.replaceAll("(?m)^\\s*[ㄱ-ㅎ]\\s*$", "")
			.replaceAll("(?m)^\\s*[A-Z]{1,3}\\s*$", "");

		// --- '용어' 단위 지능적 분할 로직 (기존과 동일) ---
		List<String> chunks = new ArrayList<>();
		StringBuilder currentChunk = new StringBuilder();
		String[] lines = cleanedText.split("\\r?\\n");

		for (String line : lines) {
			String trimmedLine = line.trim();
			if (trimmedLine.isEmpty()) continue;

			boolean isLikelyTitle = trimmedLine.length() < 50 && !trimmedLine.endsWith("다.") && !trimmedLine.endsWith(".)") && !trimmedLine.startsWith("연관검색어");

			if (isLikelyTitle) {
				if (currentChunk.length() > 0) {
					chunks.add(currentChunk.toString().trim());
				}
				currentChunk = new StringBuilder(trimmedLine).append("\n");
			} else {
				if (currentChunk.length() > 0) {
					currentChunk.append(trimmedLine).append(" ");
				}
			}
		}
		if (currentChunk.length() > 0) {
			chunks.add(currentChunk.toString().trim());
		}

		chunks.removeIf(chunk -> chunk.length() < 50);

		log.info("PDF에서 총 {}개의 의미 있는 텍스트 조각(Chunk)을 추출하고 정제했습니다.", chunks.size());

		// --- [수정된 로직] 임베딩 및 저장 로직 (배치 처리) ---
		log.info("총 {}개의 청크에 대해 '배치 병렬 임베딩'을 시작합니다. (Executor: {})", chunks.size(), taskExecutor);

		// 스레드 풀의 최대 크기(maxPoolSize)와 맞추는 것이 좋습니다.
		final int batchSize = 10;
		List<CompletableFuture<Map.Entry<String, List<Float>>>> batchFutures = new ArrayList<>();

		for (String chunk : chunks) {
			// 1. 작업을 비동기로 생성하여 배치 리스트에 추가
			batchFutures.add(
				CompletableFuture.supplyAsync(() -> createEmbedding(chunk), taskExecutor)
			);

			// 2. 배치가 꽉 차면, 이 배치가 완료될 때까지 기다린 후 결과를 저장
			if (batchFutures.size() >= batchSize) {
				log.info("임베딩 배치( {}개) 처리 및 대기...", batchFutures.size());
				processEmbeddingBatch(batchFutures);
				batchFutures.clear(); // 다음 배치를 위해 리스트 비우기
			}
		}

		// 3. 마지막으로 남아있는 작업들 처리
		if (!batchFutures.isEmpty()) {
			log.info("남아있는 마지막 임베딩 배치( {}개) 처리...", batchFutures.size());
			processEmbeddingBatch(batchFutures);
		}

		log.info("RAG 데이터 초기화 완료. 총 {}개의 벡터를 메모리에 저장했습니다.", vectorStore.size());
	}

	/**
	 * [신규 헬퍼 메서드]
	 * 임베딩 작업 배치(List<CompletableFuture>)가 모두 완료되기를 기다린 후,
	 * 그 결과(Map.Entry)를 vectorStore에 저장합니다.
	 *
	 * @param batchFutures 처리할 비동기 작업 목록
	 */
	private void processEmbeddingBatch(List<CompletableFuture<Map.Entry<String, List<Float>>>> batchFutures) {
		// 1. 배치의 모든 작업이 완료될 때까지 현재 스레드(RagAsync- 스레드)를 대기시킴
		CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();

		// 2. 완료된 작업들의 결과를 수집하여 vectorStore에 저장
		batchFutures.stream()
			.map(CompletableFuture::join) // 각 Future의 결과를 가져옴 (이미 완료됨)
			.filter(Objects::nonNull)     // 실패한 작업(null)은 필터링
			.forEach(entry -> vectorStore.put(entry.getKey(), entry.getValue()));
	}

	/**
	 * [신규 헬퍼 메서드]
	 * 청크 1개에 대한 임베딩 API 호출을 수행하는 헬퍼 메서드입니다.
	 * CompletableFuture.supplyAsync 내부에서 호출됩니다.
	 *
	 * @param chunk 임베딩할 텍스트 조각
	 * @return 성공 시 (텍스트, 벡터) Map.Entry, 실패 시 null
	 */
	private Map.Entry<String, List<Float>> createEmbedding(String chunk) {
		try {
			EmbeddingCreateParams params = EmbeddingCreateParams.builder()
				.model(EMBEDDING_MODEL)
				.input(chunk)
				.build();
			CreateEmbeddingResponse response = openAiClient.embeddings().create(params);

			if (response.data() != null && !response.data().isEmpty()) {
				List<Float> vector = response.data().get(0).embedding();
				// 성공 시 Map.Entry 객체 반환
				return Map.entry(chunk, vector);
			} else {
				log.warn("임베딩 데이터가 비어있습니다 (null or empty data). 청크: '{}...'", chunk.substring(0, Math.min(chunk.length(), 30)));
				return null;
			}
		} catch (Exception e) {
			// 개별 청크의 임베딩 실패가 전체 init을 중단시키지 않도록 예외 처리
			log.error("텍스트 조각 임베딩 중 오류 발생: '{}...'", chunk.substring(0, Math.min(chunk.length(), 50)), e);
			return null; // 실패 시 null 반환
		}
	}


	/**
	 * RAG 검색 단계: 질문 벡터와 가장 유사한 텍스트 조각을 찾습니다.
	 */
	private String findSimilarChunks(List<Float> questionVector, int topK) {
		Map<String, Double> similarityScores = new HashMap<>();
		for (Map.Entry<String, List<Float>> entry : vectorStore.entrySet()) {
			double similarity = cosineSimilarity(questionVector, entry.getValue());
			similarityScores.put(entry.getKey(), similarity);
		}
		return similarityScores.entrySet().stream()
			.sorted(Map.Entry.<String, Double>comparingByValue().reversed())
			.limit(topK)
			.map(Map.Entry::getKey)
			.collect(Collectors.joining("\n\n"));
	}

	/**
	 * 코사인 유사도 계산 헬퍼 메서드
	 */
	private double cosineSimilarity(List<Float> vec1, List<Float> vec2) {
		double dotProduct = 0.0;
		double normA = 0.0;
		double normB = 0.0;
		for (int i = 0; i < vec1.size(); i++) {
			dotProduct += vec1.get(i) * vec2.get(i);
			normA += Math.pow(vec1.get(i), 2);
			normB += Math.pow(vec2.get(i), 2);
		}
		return dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));
	}


	/**
	 * RAG 생성 단계: 비동기 파이프라인으로 변경
	 */
	@Override
	public CompletableFuture<ChatResponseDto> getChatResponse(ChatRequestDto chatRequestDto) {
		log.info("GPT 비동기 요청 시작 (RAG): {}", chatRequestDto.getQuestion());

		// --- RAG 서비스 준비 상태 확인 ---
		if (!isReady.get()) {
			log.warn("RAG 서비스가 아직 준비되지 않았습니다. (초기화 진행 중)");
			return CompletableFuture.completedFuture(
				new ChatResponseDto("시스템이 아직 준비 중입니다. 잠시 후 다시 시도해주세요.")
			);
		}

		// 1. 사용자 질문 임베딩 (I/O 작업 1) - 비동기 실행
		CompletableFuture<List<Float>> questionVectorFuture = CompletableFuture.supplyAsync(() -> {
			log.info("비동기: 질문 임베딩 시작...");
			EmbeddingCreateParams questionParams = EmbeddingCreateParams.builder()
				.model(EMBEDDING_MODEL)
				.input(chatRequestDto.getQuestion())
				.build();
			CreateEmbeddingResponse response = openAiClient.embeddings().create(questionParams);

			if (response.data() == null || response.data().isEmpty()) {
				log.warn("사용자 질문을 임베딩할 수 없습니다.");
				// 예외를 던져서 .exceptionally() 에서 처리하도록 함
				throw new RuntimeException("질문 임베딩 실패");
			}
			return response.data().get(0).embedding();
		}, taskExecutor); // Step 1에서 만든 스레드 풀 사용

		// 2. 임베딩 완료 후 -> 유사 청크 검색 (In-Memory, 빠름) -> 프롬프트 구성
		CompletableFuture<String> augmentedPromptFuture = questionVectorFuture.thenApply(questionVector -> {
			log.info("비동기: 유사 청크 검색 시작...");
			String retrievedContext = findSimilarChunks(questionVector, TOP_K_CHUNKS);
			// 로그 길이 제한 (너무 길면 로그가 지저분해짐)
			log.info("검색된 참고 자료: {}", retrievedContext.substring(0, Math.min(retrievedContext.length(), 100)) + "...");

			if (retrievedContext.trim().isEmpty()) {
				log.warn("참고할 만한 자료를 찾지 못했습니다. 일반 답변을 시도합니다.");
				return chatRequestDto.getQuestion();
			} else {
				return "아래 [참고 자료]를 바탕으로 사용자의 [질문]에 대해 답변해주세요.\n\n"
					+ "[참고 자료]\n"
					+ retrievedContext + "\n\n"
					+ "[질문]\n"
					+ chatRequestDto.getQuestion();
			}
		});

		// 3. 프롬프트 구성 완료 후 -> GPT 챗 완료 (I/O 작업 2) - 비동기 실행
		CompletableFuture<ChatResponseDto> chatResponseFuture = augmentedPromptFuture.thenApplyAsync(augmentedPrompt -> {
			log.info("비동기: GPT 챗 완료 요청 시작...");
			ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
				.model(ChatModel.GPT_4O_MINI_2024_07_18)
				.addSystemMessage(this.systemPrompt)
				.addUserMessage(augmentedPrompt)
				.maxCompletionTokens(1024)
				.build();

			ChatCompletion completion = openAiClient.chat().completions().create(params);

			if (completion.choices().isEmpty()) {
				log.warn("응답 choices가 비어있음");
				throw new RuntimeException("GPT 응답 없음");
			}
			String content = completion.choices().get(0).message().content().orElse("");
			log.info("GPT 응답 내용: {}", content.substring(0, Math.min(content.length(), 100)) + "...");
			return new ChatResponseDto(content);
		}, taskExecutor); // Step 1에서 만든 스레드 풀 사용

		// 4. 모든 예외 처리
		return chatResponseFuture.exceptionally(ex -> {
			log.error("GPT 비동기 처리 중 에러 발생: {}", ex.getMessage(), ex);
			return new ChatResponseDto("죄송합니다. 답변을 생성하는 중 오류가 발생했습니다: " + ex.getMessage());
		});
	}
}
