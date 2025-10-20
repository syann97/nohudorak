package org.scoula.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.user.domain.UserVo;

/**
 * 사용자 정보 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
@Mapper
public interface UserMapper {
	/**
	 * 이메일로 사용자를 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return UserVo 객체
	 */
	UserVo findByEmail(@Param("email") String email);

	/**
	 * 새로운 사용자를 저장합니다.
	 * @param user 저장할 사용자 정보
	 * @return 저장된 행의 수
	 */
	int save(UserVo user);

	/**
	 * 기존 사용자 정보를 수정합니다.
	 * @param user 수정할 사용자 정보
	 * @return 수정된 행의 수
	 */
	int update(UserVo user);

	/**
	 * 사용자의 개인 정보(이름, 전화번호, 생년월일)를 수정합니다.
	 * @param user 수정할 정보가 담긴 UserVo 객체 (email 필드는 WHERE절에서 사용)
	 * @return 수정된 행의 수
	 */
	int updateUserInfo(UserVo user);

	/**
	 * 이메일로 사용자를 삭제합니다.
	 * @param email 삭제할 사용자의 이메일
	 * @return 삭제된 행의 수
	 */
	int deleteByEmail(@Param("email") String email);

	/**
	 * 사용자의 마이데이터 연동 ID를 수정합니다.
	 * @param email 사용자 이메일
	 * @param connectedId 수정할 연동 ID
	 * @return 수정된 행의 수
	 */
	int updateConnectedId(@Param("email") String email, @Param("connectedId") String connectedId);

	/**
	 * 사용자의 선호 지점 ID를 수정합니다.
	 * @param email 사용자 이메일
	 * @param branchId 수정할 지점 ID
	 * @return 수정된 행의 수
	 */
	int updateBranchId(@Param("email") String email, @Param("branchId") Integer branchId);

	/**
	 * 자산 정보가 있는 전체 사용자 수를 조회합니다.
	 * @return 전체 사용자 수
	 */
	long countAllUsersWithAsset();

	/**
	 * 주어진 자산보다 더 많은 자산을 가진 사용자 수를 조회합니다.
	 * @param asset 비교 기준이 되는 자산 금액
	 * @return 기준보다 자산이 많은 사용자 수
	 */
	long countUsersWithMoreAsset(@Param("asset") Long asset);

	/**
	 * 특정 사용자의 포인트를 증가 또는 감소시킵니다.
	 *
	 * @param email 포인트를 변경할 사용자 이메일
	 * @param delta 변경할 포인트 값 (양수면 증가, 음수면 감소)
	 * @return 업데이트된 레코드 수
	 */
	int addPoint(@Param("email") String email, @Param("delta") int delta);
}
