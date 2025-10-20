package org.scoula.gpt.service;

import org.scoula.gpt.dto.ChatRequestDto;
import org.scoula.gpt.dto.ChatResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;

/**
 * GptService 인터페이스의 최종 구현체입니다.
 * Config 클래스 없이, 서비스 내에서 직접 OpenAIClient를 생성합니다.
 */
@Service
public class GptServiceImpl implements GptService {

	private static final Logger log = LoggerFactory.getLogger(GptServiceImpl.class);

	// 서비스 내에서 직접 생성하고 관리하는 OpenAIClient
	private final OpenAIClient openAiClient;

	// 하드코딩된 시스템 프롬프트
	private final String systemPrompt = "당신은 시니어분들을 위해 금융·경제 용어를 쉽게 설명하는 금융 전문가입니다. 아래 [답변 규칙]을 꼭 지켜 사용자의 질문에 답하세요.\n"
		+ "이번 작업의 목표는 [작업 목표: 예. 신속성과 효율성 / 정확성 우선]입니다.\n"
		+ "\n"
		+ "[답변 규칙]\n"
		+ "1. 금융·경제 용어는 딱 한 문장으로, 비유를 들어 쉽고 간단하게 설명하며, 어려운 용어는 괄호 안에 쉬운 뜻을 덧붙이세요(예: 뱅크런(대규모 예금 인출)).\n"
		+ "2. 말투는 '해요체'로 높이고 친근하게 답변하세요.\n"
		+ "3. 영어 약어 질문은 약어와 한글 전체 이름을 함께 표기하세요(예: GDP(국내총생산)).\n"
		+ "4. 한글 발음 질문은 정확한 영문 약어로 바꾸어 설명하세요(예: 이티에프 → ETF(상장지수펀드)).\n"
		+ "5. 투자 추천이나 금융 조언은 절대 하지 마세요.\n"
		+ "6. 답변 범위는 금융·경제 용어이며, 그 외 질문에는 \"죄송해요! 저는 금융·경제 용어에 대해서만 답변을 드릴 수 있어요.\"라고 정중히 답하세요.\n"
		+ "7. 개인적인 질문에 대한 대화는 하지 마세요.\n"
		+ "8. 금융·경제와 무관한 대화는 하지 마세요.\n"
		+ "9. 답변에서 마크다운 문법과 아이콘은 제거하세요.\n"
		+ "\n"
		+ "[불확실성 처리]\n"
		+ "- 완전히 정확하지 않더라도, 가장 합리적인 답을 가능한 한 빨리 제시한다.\n"
		+ "- 추가 확인이 필요하더라도, 우선 실행 후 그 결과를 보고한다.";

	/**
	 * 생성자에서 API 키를 주입받아 OpenAIClient를 초기화합니다.
	 * @param apiKey application-dev.properties에 정의된 openai.api.key 값
	 */
	public GptServiceImpl(@Value("${openai.api.key}") String apiKey) {
		// 1. 주입받은 API 키로 OpenAIClient를 직접 생성합니다.
		this.openAiClient = OpenAIOkHttpClient.builder()
			.apiKey(apiKey)
			.build();
		log.info("GptServiceImpl 초기화 완료: OpenAIClient 생성");
	}

	/**
	 * 사용자의 질문 DTO를 받아 GPT 모델의 답변 DTO를 반환합니다.
	 * @param chatRequestDto 사용자의 질문이 담긴 DTO
	 * @return GPT 모델의 답변이 담긴 DTO
	 */
	@Override
	public ChatResponseDto getChatResponse(ChatRequestDto chatRequestDto) {
		log.info("GPT 요청 시작: {}", chatRequestDto.getQuestion());

		try {
			ChatCompletionCreateParams params = ChatCompletionCreateParams.builder()
				.model(ChatModel.GPT_4O_MINI_2024_07_18)
				.addSystemMessage(this.systemPrompt)
				.addUserMessage(chatRequestDto.getQuestion())
				.maxCompletionTokens(1024)
				.build();

			ChatCompletion completion = openAiClient.chat().completions().create(params);
			log.info("GPT API 호출 성공: {}", completion);

			if (completion.choices().isEmpty()) {
				log.warn("응답 choices가 비어있음");
				return new ChatResponseDto("GPT 응답 없음");
			}

			String content = completion.choices().get(0).message().content().orElse("");
			log.info("GPT 응답 내용: {}", content);

			return new ChatResponseDto(content);

		} catch (Exception e) {
			e.printStackTrace();
			log.error("GPT 서비스 처리 중 에러 발생: {}", e.getMessage(), e);
			throw e;
		}
	}
}
