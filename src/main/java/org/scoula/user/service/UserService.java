package org.scoula.user.service;

import org.scoula.user.dto.MyPageResponseDto;
import org.scoula.user.dto.UserBranchNameDto;
import org.scoula.user.dto.UserDto;
import org.scoula.user.dto.UserInfoResponseDto;
import org.scoula.user.dto.UserInfoUpdateRequestDto;

/**
 * 사용자 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface UserService {
	/**
	 * 이메일로 사용자 정보를 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return 사용자 정보 DTO
	 */
	UserDto getUser(String email);

	/**
	 * 신규 사용자를 등록합니다.
	 * @param userDto 등록할 사용자 정보 DTO
	 */
	void join(UserDto userDto);

	/**
	 * 기존 사용자 정보를 수정합니다.
	 * @param email 수정할 사용자의 이메일
	 * @param userDto 수정할 정보가 담긴 DTO
	 */
	void updateUser(String email, UserDto userDto);

	/**
	 * 사용자의 기본 정보(이메일, 이름, 전화번호, 생년월일)를 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return 사용자의 기본 정보가 담긴 DTO
	 */
	UserInfoResponseDto getUserInfo(String email);

	/**
	 * 사용자의 개인 정보(이름, 전화번호, 생년월일)를 수정합니다.
	 * @param email 수정할 사용자의 이메일
	 * @param requestDto 수정할 정보가 담긴 DTO
	 */
	void updateUserInfo(String email, UserInfoUpdateRequestDto requestDto);

	/**
	 * 사용자의 마이데이터 연동 ID를 수정합니다.
	 * @param email 사용자 이메일
	 * @param connectedId 수정할 연동 ID
	 */
	void updateConnectedId(String email, String connectedId);

	UserBranchNameDto getBranchInfo(String email);

	/**
	 * 사용자의 선호 지점 ID를 수정합니다.
	 * @param email 사용자 이메일
	 * @param branchId 수정할 지점 ID
	 */
	void updateBranchId(String email, Integer branchId);

	/**
	 * 회원 탈퇴를 처리합니다.
	 * @param email 탈퇴할 사용자의 이메일
	 */
	void withdrawUser(String email);

	/**
	 * 사용자 ID를 기반으로 마이페이지 데이터를 조회하여 DTO로 반환합니다.
	 * @param email 사용자의 고유 식별자 (예: 이메일)
	 * @return 마이페이지에 필요한 데이터가 담긴 DTO
	 */
	MyPageResponseDto getMyPageData(String email);

	/**
	 * 특정 사용자의 포인트를 증가 또는 감소시킵니다.
	 *
	 * @param email 포인트를 변경할 사용자 이메일
	 * @param delta 변경할 포인트 값 (양수면 증가, 음수면 감소)
	 */
	void addPoint(String email, int delta);
}
