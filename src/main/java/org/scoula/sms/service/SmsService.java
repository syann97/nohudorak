package org.scoula.sms.service;

import org.scoula.sms.dto.SmsRequestDto;
import org.scoula.sms.dto.SmsResponseDto;

/**
 * SMS 발송 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface SmsService {
	/**
	 * 주어진 요청 정보를 바탕으로 단일 SMS 메시지를 발송합니다.
	 * @param request SMS 발송에 필요한 정보(수신자 번호, 메시지 내용 등)
	 * @return SMS 발송 결과 응답 DTO
	 */
	SmsResponseDto sendSms(SmsRequestDto request);
}
