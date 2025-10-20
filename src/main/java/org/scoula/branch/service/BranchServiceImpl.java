package org.scoula.branch.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scoula.branch.dto.BranchDto;
import org.scoula.branch.mapper.BranchMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * BranchService의 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements BranchService {

	private final BranchMapper branchMapper;

	/**
	 * 모든 지점 목록을 조회하여 DTO 리스트로 변환 후 반환합니다.
	 */
	@Override
	public List<BranchDto> getAllBranches() {
		return branchMapper.getAllBranches().stream()
			.map(BranchDto::of)
			.collect(Collectors.toList());
	}

	/**
	 * 지점 ID로 특정 지점 정보를 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 해당 지점 정보 DTO
	 * @throws NoSuchElementException 해당 ID의 지점이 없을 경우 발생
	 */
	@Override
	public BranchDto getBranchById(Integer branchId) {
		// Optional을 사용하여 null-safe하게 처리하고, 결과가 없으면 예외를 던집니다.
		return Optional.ofNullable(branchMapper.getBranchById(branchId))
			.map(BranchDto::of)
			// 예외 메시지를 "id"에 맞게 수정
			.orElseThrow(() -> new NoSuchElementException("Branch not found with id: " + branchId));
	}

	/**
	 * 지점 ID로 지점 이름을 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 지점 이름(String)
	 * @throws NoSuchElementException 해당 ID의 지점이 없을 경우 발생
	 */
	@Override
	public String getBranchNameById(int branchId) {
		// mapper를 통해 branchId로 이름을 조회하고, 결과가 없을 경우 예외를 던집니다.
		return Optional.ofNullable(branchMapper.findBranchNameById(branchId))
			.orElseThrow(() -> new NoSuchElementException("Branch not found with id: " + branchId));
	}
}