package org.scoula.sms.service;

import org.scoula.sms.dto.SmsRequestDto;
import org.scoula.sms.dto.SmsResponseDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;

import lombok.extern.log4j.Log4j2;

/**
 * CoolSMS 서비스를 이용하여 SMS를 발송하는 서비스 구현 클래스
 */
@Service
@Log4j2
public class SmsServiceImpl implements SmsService {

	// Nurigo SDK 초기화
	private final DefaultMessageService messageService;

	// 발신번호
	// 번호는 반드시 -,* 등의 특수문자를 제거한 01012345678 형식으로 입력
	// 현재는 CoolSMS 에 등록된 번호 (01099255708) 만 설정 가능함
	@Value("${sms.from.number:INSERT_SENDER_NUMBER}")
	private String fromNumber;

	/**
	 * 생성자에서 application-dev.properties의 API 키와 시크릿 키를 주입받아
	 * Nurigo 메시지 서비스를 초기화합니다.
	 * @param apiKey CoolSMS API 키
	 * @param apiSecret CoolSMS API 시크릿 키
	 */
	public SmsServiceImpl(@Value("${sms.api.key:INSERT_API_KEY}") String apiKey,
		@Value("${sms.api.secret:INSERT_API_SECRET_KEY}") String apiSecret) {

		// Profile 및 API 키 로딩 상태 확인용 로그
		log.info("=== SMS Service 초기화 시작 ===");
		log.info("SMS API Key 로딩됨: [{}] (길이: {})", apiKey, apiKey.length());
		log.info("SMS API Secret 로딩됨: [{}...] (길이: {})",
			apiSecret.substring(0, Math.min(8, apiSecret.length())), apiSecret.length());

		this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
		log.info("SMS Service 초기화 완료");
	}

	/**
	 * 단일 메세지 발송
	 * 한글 45자, 영자 90자 이하 입력되면 자동으로 SMS 타입
	 * @param request SMS 발송에 필요한 정보를 담은 DTO
	 * @return SMS 발송 결과 DTO
	 */
	@Override
	public SmsResponseDto sendSms(SmsRequestDto request) {
		SmsResponseDto response = new SmsResponseDto();

		try {
			// 예약 정보를 바탕으로 메시지를 생성합니다.
			String messageText = createReservationMessage(request);

			Message message = new Message();
			// 발신번호 설정
			message.setFrom(fromNumber);
			// 수신번호 설정
			message.setTo(request.getPhoneNumber());
			// ⭐️ 문자 내용을 입력합니다! ⭐
			message.setText(messageText);

			SingleMessageSentResponse sentResponse = messageService.sendOne(new SingleMessageSendingRequest(message));

			response.setSuccess(true);
			response.setMessage("SMS 전송 성공");
			assert sentResponse != null;
			response.setMessageId(sentResponse.getMessageId());

			log.info("SMS 전송 성공: " + sentResponse);

		} catch (Exception e) {
			response.setSuccess(false);
			response.setMessage("SMS 전송 실패: " + e.getMessage());
			response.setErrorCode("SMS_SEND_ERROR");

			log.error("SMS 전송 실패", e);
		}

		return response;
	}

	/**
	 * 예약 정보를 바탕으로 발송할 SMS 메시지 내용을 생성합니다.
	 * @param request 예약 정보가 담긴 DTO
	 * @return 완성된 메시지 문자열
	 */
	// 예약 메세지 템플릿 - 추후 개선
	private String createReservationMessage(SmsRequestDto request) {
		StringBuilder sb = new StringBuilder();

		// userName이 없으면 "고객님"으로 처리
		String userName = (request.getUserName() != null && !request.getUserName().trim().isEmpty())
			? request.getUserName() : "고객";

		sb.append("[ 예약 완료 알림 ]\n");
		sb.append("안녕하세요 ").append(userName).append("님!\n\n");
		sb.append("방문 예약이 완료되었습니다.\n\n");
		sb.append("▶ 상품명: ").append(request.getProductName()).append("\n");
		sb.append("▶ 지점: ").append(request.getBranchName()).append("\n");
		sb.append("▶ 예약일시: ").append(request.getReservationDate())
			.append(" ").append(request.getReservationTime()).append("\n\n");

		// 필요 서류 추후 수정
		sb.append("방문 시 [ 신분증 ] 을 지참해주세요.\n");
		sb.append("감사합니다.");

		return sb.toString();
	}
}
