package org.scoula.gpt.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
import org.apache.pdfbox.text.TextPosition;
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

	private static final int MAX_CHUNK_CHAR_LIMIT = 10000;

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

	// ===================================================================================
	// === [수정] Layout-Aware 파싱을 위한 비공개 정적 내부 클래스 ===
	// ===================================================================================
	/**
	 * PDF의 레이아웃(폰트 크기, Y좌표)을 분석하여 Markdown을 생성하는 커스텀 Stripper.
	 * [V3 개선]
	 * 1. '상태 플래그'(contentStarted, contentEnded)를 도입하여 목차(TOC)와 부록(집필자 등)을 파싱 단계에서부터 제외.
	 * 2. Y좌표를 이용한 헤더/푸터 영역 필터링 로직 추가.
	 * 3. 목차의 "・・・・・・" 패턴을 명시적으로 필터링.
	 */
	private static class LayoutAwareStripper extends PDFTextStripper {

		private final StringBuilder markdown = new StringBuilder();
		private float lastY = -1;
		private float lastFontSize = -1;
		private boolean isNewLine = true;

		// --- [개선] 상태 플래그 및 마커 ---
		/**
		 * true가 되기 전까지 모든 텍스트를 무시 (목차 스킵용)
		 */
		private boolean contentStarted = false;
		/**
		 * true가 되면 이후 모든 텍스트를 무시 (부록 스킵용)
		 */
		private boolean contentEnded = false;
		/**
		 * 본문 시작을 알리는 첫 번째 용어
		 */
		private static final String START_MARKER = "가계부실위험지수(HDRI)";
		/**
		 * 본문 종료(부록 시작)를 알리는 마커
		 */
		private static final String END_MARKER = "경제금융용어 700선  집필자";
		// ---------------------------------

		// [튜닝 포인트 1] PDF 원본 확인 후, 본문 폰트 크기보다는 크고 제목 폰트 크기보다는 작은 값으로 조정
		private static final float HEADING_FONT_SIZE_THRESHOLD = 11.5f;

		// [튜닝 포인트 2] 단락 구분을 위한 Y좌표 간격 (폰트 크기의 1.5배)
		private static final float PARAGRAPH_SPACE_THRESHOLD = 1.5f;

		// [튜닝 포인트 3] 헤더/푸터 영역 Y좌표 (A4 페이지 842pt 기준)
		private static final float HEADER_Y_LIMIT = 70.0f;
		private static final float FOOTER_Y_LIMIT = 770.0f; // 페이지 번호 '1', '2' 등이 찍히는 Y좌표

		public LayoutAwareStripper() throws IOException {
			super();
			// [필수!] PDFBox가 텍스트를 시각적 Y/X 좌표 순서로 정렬하도록 강제
			setSortByPosition(true);
		}

		/**
		 * 파싱 완료된 Markdown 텍스트를 반환합니다.
		 */
		public String getMarkdownResult() {
			return markdown.toString().trim();
		}

		/**
		 * PDF의 텍스트 조각(TextPosition)을 하나씩 처리하며 Markdown을 생성합니다.
		 * (상태 관리 로직이 적용된 최종 버전)
		 */
		@Override
		protected void writeString(String text, List<TextPosition> textPositions) throws IOException {

			// 1. Guard Clauses (유효성 검사)
			if (textPositions.isEmpty()) {
				return;
			}
			String trimmedText = text.trim();
			if (trimmedText.isEmpty()) {
				return;
			}

			// --- [개선] 상태 머신(State Machine) 로직 ---
			// 2-1. [상태 3] 본문이 종료되었다면, 이후 모든 텍스트 무시
			if (contentEnded) {
				return;
			}

			// 2-2. 본문 종료 마커("집필자")를 감지하면, 상태를 3으로 변경하고 현재 텍스트 무시
			if (trimmedText.contains(END_MARKER)) {
				log.info("본문 종료 마커('{}') 감지. 이후 텍스트를 무시합니다.", END_MARKER);
				contentEnded = true;
				return;
			}

			// 2-3. [상태 1] 아직 본문이 시작되지 않았다면,
			if (!contentStarted) {
				// 2-4. 본문 시작 마커(첫 용어)를 감지하면 상태를 2로 변경 (파싱 시작)
				if (trimmedText.equals(START_MARKER)) {
					log.info("본문 시작 마커('{}') 감지. 파싱을 시작합니다.", START_MARKER);
					contentStarted = true;
					// (fall-through하여 이 텍스트부터 파싱을 시작함)
				} else {
					// 2-5. 시작 마커가 아니면 (머리말, 목차 등) 현재 텍스트 무시
					log.trace("본문 시작 전 텍스트 무시: {}", trimmedText);
					return;
				}
			}
			// --- [상태 2] (contentStarted = true, contentEnded = false) ---
			// 3. 노이즈 필터링 (TOC, 헤더, 푸터)
			// 3-1. [개선] 목차(TOC)의 "...." 패턴이 포함된 라인 폐기
			// (상태 관리 로직으로 대부분 걸러지지만, 만약을 위한 이중 방어)
			if (trimmedText.contains("・・・・・・")) {
				log.trace("목차(TOC) 패턴 감지. 텍스트 무시: {}", trimmedText);
				return;
			}

			// 3-2. [개선] 헤더/푸터 Y좌표 영역 텍스트 폐기
			TextPosition firstPos = textPositions.get(0);
			float currentY = firstPos.getY();
			float currentFontSize = firstPos.getFontSizeInPt();

			if (currentY < HEADER_Y_LIMIT || currentY > FOOTER_Y_LIMIT) {
				log.trace("헤더/푸터 Y좌표({}pt) 텍스트 무시: {}", currentY, trimmedText);
				return; // (페이지 번호 '1', '2', '3'... 등이 여기서 걸러짐)
			}

			// 4. 줄바꿈(isNewLine) 여부 판단 및 줄바꿈/공백 삽입
			if (lastY == -1) { // 문서 또는 페이지의 첫 시작
				isNewLine = true;
			} else if (Math.abs(currentY - lastY) > 1.0f) { // Y좌표가 1.0pt 이상 변경됨 (새 줄)
				isNewLine = true;

				if (Math.abs(currentY - lastY) > (currentFontSize * PARAGRAPH_SPACE_THRESHOLD)) {
					if (markdown.length() > 0 && !markdown.toString().endsWith("\n\n")) {
						markdown.append("\n\n"); // 새 문단
					}
				} else { // 단순 줄바꿈
					if (markdown.length() > 0 && !markdown.toString().endsWith("\n") && !markdown.toString().endsWith("\n\n")) {
						markdown.append("\n");
					}
				}
			} else { // Y좌표가 거의 동일 (같은 줄)
				isNewLine = false;
				if (markdown.length() > 0 && !markdown.toString().endsWith(" ") && !markdown.toString().endsWith("\n")) {
					markdown.append(" "); // 같은 줄 단어 사이 공백
				}
			}

			// 5. "Junk Content" 식별 (RAG에 불필요한 색인 문자 등)
			boolean isJunkContent = trimmedText.matches("^[ㄱ-ㅎ]$") || // "ㄱ", "ㄴ", "ㄷ"...
				trimmedText.matches("^[A-Z]$");    // "A", "B", "C"...

			// 6. 텍스트 내용 추가 (핵심 로직)
			if (isNewLine && currentFontSize > HEADING_FONT_SIZE_THRESHOLD && !isJunkContent) {
				// Case 1: [진짜 제목] (새 줄 + 큰 폰트 + Junk 아님)
				if (markdown.length() > 0 && !markdown.toString().endsWith("\n\n")) {
					if (markdown.toString().endsWith("\n")) {
						markdown.setLength(markdown.length() - 1); // 기존 \n 제거
					}
					markdown.append("\n\n");
				}
				markdown.append("## ").append(trimmedText);

			} else if (isNewLine && isJunkContent) {
				// Case 2: [가짜 제목/내용] (새 줄 + Junk 임)
				log.trace("Junk 헤더('{}') 무시", trimmedText);
				if (markdown.toString().endsWith("\n\n")) {
					markdown.setLength(markdown.length() - 2);
				} else if (markdown.toString().endsWith("\n")) {
					markdown.setLength(markdown.length() - 1);
				}
				// (텍스트는 append하지 않음)

			} else if (isNewLine) {
				// Case 3: [일반 텍스트 줄 시작]
				markdown.append(trimmedText);

			} else {
				// Case 4: [같은 줄 텍스트]
				markdown.append(text); // trim 안함
			}

			// 7. 다음 비교를 위해 마지막 위치/폰트 정보 업데이트
			if (!(isNewLine && isJunkContent)) {
				lastY = textPositions.get(textPositions.size() - 1).getY();
				lastFontSize = currentFontSize;
			}
		}

		// 페이지가 끝날 때마다 Y좌표 리셋
		@Override
		protected void writePageEnd() {
			// [개선] 본문이 시작된 후에만 페이지 구분을 추가
			if (contentStarted && !contentEnded) {
				markdown.append("\n\n");
			}
			lastY = -1;
			lastFontSize = -1;
		}
	}

	/**
	 * RAG 데이터 준비 단계: Layout-Aware 파싱 및 '하이브리드' Semantic Chunking
	 * [V3 수정]
	 * 1. 상태 관리가 포함된 LayoutAwareStripper V3 호출
	 * 2. 청킹 완료 후, 본문과 관련 없는 부록/메타데이터 청크를 필터링하는 로직 추가
	 *
	 * @param filePath PDF 파일 경로
	 * @throws IOException 파일 읽기 실패 시
	 */
	private void initializeRagData(String filePath) throws IOException {
		log.info("RAG 데이터 초기화를 (비동기) 시작합니다. (Layout-Aware + State Machine 파싱 방식) 경로: {}", filePath);
		Resource ragResource = resourceLoader.getResource(filePath);

		if (!ragResource.exists()) {
			log.error("지정된 경로에 PDF 파일이 없습니다: {}", filePath);
			throw new FileNotFoundException("PDF 파일을 찾을 수 없습니다.");
		}

		// 1단계: [개선된] Layout-Aware + State Machine 파싱
		String markdownText;
		try (PDDocument document = PDDocument.load(ragResource.getInputStream())) {
			log.info("1단계: Layout-Aware Stripper (State Machine) 파싱을 시작합니다...");
			// GptServiceImpl의 private static inner class로 LayoutAwareStripper가 정의되어 있어야 함
			LayoutAwareStripper stripper = new LayoutAwareStripper();
			stripper.getText(document);
			markdownText = stripper.getMarkdownResult();
		}

		// (디버깅 코드 - 파일명 변경)
		try {
			String dumpFileName = "structured_markdown_dump_v3.txt"; // 새 이름으로 저장
			java.nio.file.Path dumpPath = Paths.get(dumpFileName);
			Files.writeString(dumpPath, markdownText);
			log.info("개선된 파싱 결과(Markdown)를 다음 경로에 파일로 저장했습니다: {}", dumpPath.toAbsolutePath());
		} catch (IOException e) {
			log.warn("Markdown 덤프 파일 저장 중 오류 발생", e);
		}

		// 2단계: '하이브리드' Markdown 기반 Semantic Chunking
		log.info("2단계: '하이브리드' Markdown 기반 Semantic Chunking을 시작합니다...");

		// 1차 분할: '## ' (제목) 기준으로 분할
		String[] rawChunks = markdownText.split("(?m)(?=\n##\\s)");

		List<String> chunks = new ArrayList<>(); // 1차 청크 리스트
		final String PARAGRAPH_SPLITTER = "\n\n";

		for (String rawChunk : rawChunks) {
			String cleanedChunk = rawChunk.trim();

			// 2.1: 청크가 최대 길이(MAX_CHUNK_CHAR_LIMIT)를 초과하는지 확인
			if (cleanedChunk.length() > MAX_CHUNK_CHAR_LIMIT) {
				log.warn("청크가 최대 길이( {}자)를 초과했습니다 (현재 {}자). 문단 기준으로 2차 분할합니다. (시작: '{}...')",
					MAX_CHUNK_CHAR_LIMIT, cleanedChunk.length(), cleanedChunk.substring(0, 50).replaceAll("\\r?\\n", " "));

				// 2.2: 문단(`\n\n`) 기준으로 2차 분할
				String[] subChunks = cleanedChunk.split(PARAGRAPH_SPLITTER);

				if (subChunks.length > 1) {
					// 2.3: 첫 번째 청크는 '제목' (예: "## 용어 A")
					String titleHeader = subChunks[0];
					StringBuilder currentSubChunkBuilder = new StringBuilder(titleHeader);

					// 2.4: 나머지 문단들을 순회하며 제목을 붙여 하위 청크 생성
					for (int i = 1; i < subChunks.length; i++) {
						String paragraph = subChunks[i].trim();
						if (paragraph.isEmpty()) continue; // 빈 문단 스킵

						// 2.5: 문단을 붙였을 때 최대 길이를 넘는지 확인
						if (currentSubChunkBuilder.length() + paragraph.length() + PARAGRAPH_SPLITTER.length() > MAX_CHUNK_CHAR_LIMIT) {
							// 넘으면, 지금까지 만든 청크를 저장하고 새 청크 시작
							String finalSubChunk = postProcessChunk(currentSubChunkBuilder.toString());
							if (isValidChunk(finalSubChunk)) {
								chunks.add(finalSubChunk);
							}
							// 새 청크는 다시 '제목' + '현재 문단'으로 시작
							currentSubChunkBuilder = new StringBuilder(titleHeader).append(PARAGRAPH_SPLITTER).append(paragraph);
						} else {
							// 넘지 않으면, 현재 청크에 문단을 계속 추가
							currentSubChunkBuilder.append(PARAGRAPH_SPLITTER).append(paragraph);
						}
					}
					// 2.6: 마지막에 남아있는 하위 청크도 추가
					String finalSubChunk = postProcessChunk(currentSubChunkBuilder.toString());
					if (isValidChunk(finalSubChunk)) {
						chunks.add(finalSubChunk);
					}
				} else {
					// 2.7: 문단 분할이 불가능한 거대 청크 (최악의 경우)
					log.error("청크가 너무 길지만 문단( '{}')으로 분할할 수 없습니다. 강제로 {}자에서 자릅니다. (시작: '{}...')",
						PARAGRAPH_SPLITTER, MAX_CHUNK_CHAR_LIMIT, cleanedChunk.substring(0, 50).replaceAll("\\r?\\n", " "));
					String truncatedChunk = cleanedChunk.substring(0, MAX_CHUNK_CHAR_LIMIT);
					String finalTruncatedChunk = postProcessChunk(truncatedChunk);
					// 유효성(## 시작)도 통과 못하면 그냥 강제로 추가
					chunks.add(finalTruncatedChunk);
				}

			} else if (!cleanedChunk.isEmpty()) {
				// 2.8: 청크가 길지 않은 경우 (정상)
				String finalChunk = postProcessChunk(cleanedChunk); // 정제
				if (isValidChunk(finalChunk)) { // 유효성 검사
					chunks.add(finalChunk);
				}
			}
		}
		// --- 2단계(하이브리드 청킹) 끝 ---

		// [개선] 3단계: 청크 후처리 (Junk 청크 필터링)
		// "## ABC", "## 경제금융용어 700선  집필자" 등 본문이 아닌 청크를 제거합니다.
		List<String> finalFilteredChunks = chunks.stream()
			.filter(chunk ->
				!chunk.startsWith("## ABC") &&          // "ABC" 섹션 제외
					!chunk.startsWith("## 경제금융용어 700선  집필자") // "집필자" 섹션 제외
			)
			.collect(Collectors.toList());

		log.info("Markdown 기반 청크 분할 완료. (1차: {}개) -> 후처리 필터링 -> (최종: {}개)", chunks.size(), finalFilteredChunks.size());

		// 4단계: 비동기 스트리밍 임베딩
		log.info("총 {}개의 청크에 대해 '비동기 스트리밍 임베딩'을 시작합니다. (Executor: {})", finalFilteredChunks.size(), taskExecutor);

		List<CompletableFuture<Void>> allProcessingFutures = new ArrayList<>();

		// [수정] 'finalFilteredChunks' 리스트를 사용
		for (String chunk : finalFilteredChunks) {
			CompletableFuture<Map.Entry<String, List<Float>>> embeddingFuture =
				CompletableFuture.supplyAsync(() -> createEmbedding(chunk), taskExecutor);

			CompletableFuture<Void> processingFuture = embeddingFuture.thenAccept(entry -> {
				if (entry != null) {
					vectorStore.put(entry.getKey(), entry.getValue());
					String chunkText = entry.getKey();
					log.info("  > [비동기 저장 완료] 청크: {}", chunkText.substring(0, Math.min(chunkText.length(), 70)).replaceAll("\\r?\\n", " "));
				} else {
					log.warn("임베딩 결과가 null이므로 저장을 건너뜁니다. (원본 청크: {}...)", chunk.substring(0, Math.min(chunk.length(), 30)));
				}
			});

			allProcessingFutures.add(processingFuture);
		}

		log.info("모든 임베딩+저장 작업(총 {}개)이 'taskExecutor'에 제출되었습니다. 전체 완료를 기다립니다...", allProcessingFutures.size());

		try {
			CompletableFuture.allOf(allProcessingFutures.toArray(new CompletableFuture[0])).join();
		} catch (Exception e) {
			log.error("임베딩 작업 대기 중 예외 발생", e);
		}

		log.info("RAG 데이터 초기화 완료. 총 {}개의 벡터를 메모리에 저장했습니다.", vectorStore.size());
	}

	/**
	 * [헬퍼 메서드 1]
	 * 청크에서 불필요한 공백, 머리글/꼬리글 등을 제거합니다.
	 */
	private String postProcessChunk(String chunk) {
		return chunk
			.replaceAll("(?m)^경제금융용어 700선.*$", "") // 머리글/꼬리글
			.replaceAll("(?m)^[ivxlcdm\\d]+\\s*$", "") // 페이지 번호
			.replaceAll("\n\\s*\n+", "\n\n") // 과도한 빈 줄
			.trim();
	}

	/**
	 * [헬퍼 메서드 2]
	 * 청크가 RAG에 사용하기에 유효한지(제목으로 시작하는지, 최소 길이를 넘는지) 검사합니다.
	 */
	private boolean isValidChunk(String chunk) {
		// "## " (4자) + 최소한의 제목/내용 (예: 20자)
		boolean valid = chunk.length() > 24 && chunk.startsWith("## ");
		if (!valid && !chunk.isEmpty()) {
			log.debug("너무 짧거나 헤딩이 없어 제외된 청크: '{}...'", chunk.substring(0, Math.min(chunk.length(), 50)));
		}
		return valid;
	}

	/**
	 * [헬퍼 메서드 3]
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
