package org.scoula.asset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.asset.domain.AssetStatusVo;

/**
 * 자산 현황(AssetStatus) 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
@Mapper
public interface AssetStatusMapper {

	/**
	 * 특정 사용자의 자산 현황 요약 정보를 조회합니다. (카테고리별 합계)
	 * @param email 사용자 이메일
	 * @return 자산 현황 요약 리스트
	 */
	List<AssetStatusVo> findAssetStatusSummaryByEmail(String email);

	/**
	 * 특정 사용자의 모든 자산 목록을 조회합니다.
	 * @param email 사용자 이메일
	 * @return 자산 리스트
	 */
	List<AssetStatusVo> findAssetStatusByEmail(String email);

	/**
	 * 자산 ID로 특정 자산 정보를 조회합니다.
	 * @param id 자산 ID
	 * @return 자산 정보 객체
	 */
	AssetStatusVo findAssetStatusById(Integer id);

	/**
	 * 새로운 자산 정보를 데이터베이스에 추가합니다.
	 * @param assetStatus 추가할 자산 정보
	 */
	void insertAssetStatus(AssetStatusVo assetStatus);

	/**
	 * 기존 자산 정보를 수정합니다.
	 * @param assetStatus 수정할 자산 정보
	 * @return 수정된 행의 수
	 */
	int updateAssetStatus(AssetStatusVo assetStatus);

	/**
	 * 특정 자산 정보를 삭제합니다.
	 * @param assetId 삭제할 자산의 ID
	 * @param email 삭제를 요청한 사용자의 이메일 (소유자 확인용)
	 * @return 삭제된 행의 수
	 */
	int deleteAssetStatus(@Param("assetId") Integer assetId, @Param("email") String email);

	/**
	 * 특정 사용자의 모든 자산 정보를 삭제합니다. (주로 회원 탈퇴 시 사용)
	 * @param email 사용자 이메일
	 * @return 삭제된 행의 수
	 */
	int deleteByEmail(@Param("email") String email);
}