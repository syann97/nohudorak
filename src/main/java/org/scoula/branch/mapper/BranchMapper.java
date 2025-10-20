package org.scoula.branch.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.scoula.branch.domain.BranchVo;

/**
 * 지점 정보 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
public interface BranchMapper {
	/**
	 * 모든 지점 목록을 조회합니다.
	 * @return 지점 정보(BranchVo) 리스트
	 */
	List<BranchVo> getAllBranches();

	/**
	 * 지점 ID로 특정 지점 정보를 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 해당 지점 정보(BranchVo) 객체
	 */
	BranchVo getBranchById(@Param("branchId") Integer branchId);

	/**
	 * ID를 통해 지점 이름을 조회합니다.
	 * @param branchId 조회할 지점의 ID
	 * @return 지점 이름(String)
	 */
	String findBranchNameById(int branchId);

	/**
	 * ID에 해당하는 지점이 존재하는지 확인합니다.
	 * @param branchId 확인할 지점 ID
	 * @return 존재하면 true, 그렇지 않으면 false
	 */
	boolean existsById(Integer branchId);
}
