package org.scoula.View.preference.service;

import org.scoula.View.preference.dto.PreferenceRequestDto;

/**
 * 사용자 성향 설문 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface PreferenceService {
	/**
	 * 사용자가 제출한 설문 답변을 바탕으로 투자 성향을 설정합니다.
	 * @param requestDto 사용자의 설문 답변이 담긴 DTO
	 * @param userEmail 성향을 설정할 사용자의 이메일
	 */
	void setUserPreference(PreferenceRequestDto requestDto,String userEmail);
}