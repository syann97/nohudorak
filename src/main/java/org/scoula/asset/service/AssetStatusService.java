package org.scoula.asset.service;

import java.util.List;

import org.scoula.asset.domain.AssetStatusVo;
import org.scoula.asset.dto.AssetStatusIdDto;
import org.scoula.asset.dto.AssetStatusRequestDto;
import org.scoula.asset.dto.AssetStatusResponseDto;
import org.scoula.asset.dto.AssetStatusSummaryDto;

public interface AssetStatusService {

	/**
	 * 특정 사용자의 자산 상태 목록을 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return 자산 상태 응답 DTO 리스트
	 */
	List<AssetStatusResponseDto> getAssetStatusByEmail(String email);

	/**
	 * 특정 사용자의 자산 상태를 요약(카테고리별 합계 등)으로 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return 자산 상태 요약 DTO 리스트
	 */
	List<AssetStatusSummaryDto> getAssetStatusSummaryByEmail(String email);

	/**
	 * 새로운 자산 상태를 추가합니다.
	 *
	 * @param email 사용자 이메일
	 * @param requestDto 자산 상태 요청 DTO
	 * @return 생성된 자산 ID DTO
	 */
	AssetStatusIdDto addAssetStatus(String email, AssetStatusRequestDto requestDto);

	/**
	 * 특정 자산 상태를 수정합니다.
	 *
	 * @param assetId 수정할 자산 ID
	 * @param email   사용자 이메일 (소유자 검증용)
	 * @param requestDto 수정할 자산 상태 요청 DTO
	 */
	void updateAssetStatus(Integer assetId, String email, AssetStatusRequestDto requestDto);

	/**
	 * 특정 자산 상태를 삭제합니다.
	 *
	 * @param assetId 삭제할 자산 ID
	 * @param email   사용자 이메일 (소유자 검증용)
	 */
	void deleteAssetStatus(Integer assetId, String email);

	/**
	 * 특정 사용자의 모든 자산 목록을 상세 정보(Vo) 그대로 조회합니다.
	 *
	 * @param email 사용자 이메일
	 * @return AssetStatusVo 객체 리스트
	 */
	List<AssetStatusVo> getFullAssetStatusByEmail(String email);
}
