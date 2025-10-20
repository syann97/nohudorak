package org.scoula.user.service;

/**
 * 다른 서비스(예: 자산 서비스)에서 사용자 자산 정보를 업데이트할 수 있도록
 * 역할을 분리하기 위한 함수형 인터페이스
 */
@FunctionalInterface
public interface UserAssetUpdater {
	/**
	 * 특정 사용자의 총 자산을 주어진 금액만큼 증감시킵니다.
	 * @param email 사용자 이메일
	 * @param amount 변경할 자산 금액 (증가는 양수, 감소는 음수)
	 */
	void updateUserAsset(String email, long amount);
}
