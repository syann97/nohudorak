package org.scoula.branch.service;

import java.util.List;

import org.scoula.branch.dto.BranchDto;

/**
 * 지점 정보 관련 비즈니스 로직을 처리하는 서비스 인터페이스
 */
public interface BranchService {
	/**
	 * 모든 지점 목록을 조회합니다.
	 * @return 지점 정보 DTO 리스트
	 */
	List<BranchDto> getAllBranches();

	/**
	 * 지점 ID로 특정 지점 정보를 조회합니다.
	 * @param branchId 조회할 지점의 ID (잘못된 변수명 branchName -> branchId 수정)
	 * @return 해당 지점 정보 DTO
	 */
	BranchDto getBranchById(Integer branchId);

	/**
	 * 지점 ID로 지점 이름을 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 지점 이름(String)
	 */
	String getBranchNameById(int branchId);
}