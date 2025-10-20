package org.scoula.gift.service;

import org.scoula.gift.dto.GiftPageResponseDto;
import org.scoula.gift.dto.RecipientRequestDto;
import org.scoula.gift.dto.RecipientResponseDto;

/**
 * 수증자(Recipient) 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface RecipientService {
	/**
	 * 새로운 수증자 정보를 생성합니다.
	 * @param requestDto 생성할 수증자 정보가 담긴 DTO
	 * @param email 수증자를 등록하는 사용자의 이메일
	 * @return 생성된 수증자 정보 응답 DTO
	 */
	RecipientResponseDto createRecipient(RecipientRequestDto requestDto, String email);

	/**
	 * 특정 수증자의 정보를 조회합니다.
	 * @param recipientId 조회할 수증자의 ID
	 * @param email 정보를 조회하는 사용자의 이메일 (소유권 확인용)
	 * @return 조회된 수증자 정보 응답 DTO
	 */
	RecipientResponseDto findRecipientByIdAndEmail(Integer recipientId, String email);

	/**
	 * 기존 수증자의 정보를 수정합니다.
	 * @param recipientId 수정할 수증자의 ID
	 * @param requestDto 수정할 정보가 담긴 DTO
	 * @param email 정보를 수정하는 사용자의 이메일 (소유권 확인용)
	 * @return 수정된 수증자 정보 응답 DTO
	 */
	RecipientResponseDto updateRecipient(Integer recipientId, RecipientRequestDto requestDto, String email);

	/**
	 * 특정 수증자의 정보를 삭제합니다.
	 * @param recipientId 삭제할 수증자의 ID
	 * @param email 정보를 삭제하는 사용자의 이메일 (소유권 확인용)
	 * @return 삭제 성공 시 true, 실패 시 false
	 */
	boolean deleteRecipient(Integer recipientId, String email);

	/**
	 * 특정 사용자의 증여세 계산 페이지에 필요한 모든 데이터를 조회합니다.
	 * @param email 데이터를 조회하는 사용자의 이메일
	 * @return 증여 계산 페이지 데이터 DTO
	 */
	GiftPageResponseDto getGiftPageData(String email);
}
