package org.scoula.asset.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.scoula.asset.domain.AssetStatusVo;
import org.scoula.asset.dto.AssetStatusIdDto;
import org.scoula.asset.dto.AssetStatusRequestDto;
import org.scoula.asset.dto.AssetStatusResponseDto;
import org.scoula.asset.dto.AssetStatusSummaryDto;
import org.scoula.asset.mapper.AssetStatusMapper;
import org.scoula.exception.AssetNotFoundException;
import org.scoula.recommend.service.CustomRecommendService;
import org.scoula.user.dto.UserDto;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
@RequiredArgsConstructor
@Transactional
public class AssetStatusServiceImpl implements AssetStatusService {

	private final AssetStatusMapper assetStatusMapper;
	private final UserService userService;
	private final CustomRecommendService customRecommendService;

	// 자산 카테고리별 가중치 맵. 이 가중치는 사용자 자산 비중 계산에 사용됩니다.
	// 1: 부동산, 2: 예적금, 3: 현금, 4: 주식 및 펀드, 5: 사업체 및 지분, 6: 기타
	private static final Map<String, Double> assetWeights = Map.of(
		"1", 0.4,
		"2", 1.0,
		"3", 0.7,
		"4", -1.0,
		"5", -0.8,
		"6", 0.0
	);

	/**
	 * 사용자의 모든 자산 정보를 요약하고, 총 자산 및 자산 비중을 계산하여 사용자 정보를 업데이트합니다.
	 * 이 메서드는 자산 추가, 수정, 삭제 후 호출됩니다.
	 *
	 * @param userEmail 자산 정보가 변경된 사용자의 이메일
	 */
	private void updateUserAssetSummary(String userEmail) {
		// 1. 자산 목록을 DB에서 한 번만 조회합니다.
		List<AssetStatusVo> assets = assetStatusMapper.findAssetStatusByEmail(userEmail);

		double totalAmount = 0;
		double weightedSum = 0;

		for (AssetStatusVo vo : assets) {
			if (vo.getAmount() == null) {
				throw new IllegalArgumentException(
					"자산 금액(amount)이 null입니다. 해당 자산 삭제/변경 필요. \n assetId: " + vo.getAssetId());
			}
			double amount = vo.getAmount().doubleValue();
			totalAmount += amount;
			// 자산 카테고리 코드에 따라 가중치를 적용하여 가중치 합계를 계산합니다.
			weightedSum += amount * assetWeights.getOrDefault(vo.getAssetCategoryCode(), 0.0);
		}

		// 2. 계산을 수행합니다.
		// 총 자산액이 0일 경우 자산 비중 비율은 0으로 설정합니다.
		double assetProportionRate = (totalAmount == 0) ? 0.0 : weightedSum / totalAmount;

		// 3. 사용자 정보를 업데이트합니다.
		UserDto userDto = userService.getUser(userEmail);
		userDto.setAsset((long)totalAmount);
		userDto.setAssetProportion(assetProportionRate);
		userService.updateUser(userEmail, userDto);

		// 4. 업데이트된 자산 비중에 맞춰 추천 상품을 갱신합니다.
		customRecommendService.addCustomRecommend(userEmail);
	}

	/**
	 * 특정 사용자의 자산 목록을 조회하여 DTO 형태로 반환합니다.
	 *
	 * @param email 사용자 이메일
	 * @return DTO로 변환된 자산 상태 목록
	 */
	@Override
	public List<AssetStatusResponseDto> getAssetStatusByEmail(String email) {
		return assetStatusMapper.findAssetStatusByEmail(email).stream()
			.map(AssetStatusResponseDto::of)
			.collect(Collectors.toList());
	}

	/**
	 * 특정 사용자의 자산 정보를 자산 카테고리별로 요약하여 반환합니다.
	 *
	 * @param email 사용자 이메일
	 * @return 자산 카테고리별 합산된 요약 정보 목록
	 */
	@Override
	public List<AssetStatusSummaryDto> getAssetStatusSummaryByEmail(String email) {
		return assetStatusMapper.findAssetStatusSummaryByEmail(email).stream()
			.map(AssetStatusSummaryDto::of)
			.collect(Collectors.toList());
	}

	/**
	 * 새로운 자산 정보를 추가합니다.
	 *
	 * @param email 사용자 이메일
	 * @param requestDto 추가할 자산 정보
	 * @return 새로 생성된 자산의 ID를 담은 DTO
	 */
	@Transactional
	@Override
	public AssetStatusIdDto addAssetStatus(String email, AssetStatusRequestDto requestDto) {
		// 1. DTO를 VO로 변환하고 이메일 정보를 설정합니다.
		AssetStatusVo assetStatusVo = requestDto.toVo();
		assetStatusVo.setEmail(email);

		// 2. DB에 자산 정보를 삽입합니다. 이 과정에서 Mybatis가 생성된 assetId를 VO에 채워줍니다.
		assetStatusMapper.insertAssetStatus(assetStatusVo);

		// 3. 자산 정보 변경에 따라 사용자 자산 요약 정보를 업데이트합니다.
		updateUserAssetSummary(email);

		return new AssetStatusIdDto(assetStatusVo.getAssetId());
	}

	/**
	 * 기존 자산 정보를 업데이트합니다.
	 *
	 * @param assetId 수정할 자산의 ID
	 * @param email 자산 소유자의 이메일
	 * @param requestDto 업데이트할 자산 정보
	 */
	@Override
	public void updateAssetStatus(Integer assetId, String email, AssetStatusRequestDto requestDto) {
		// DTO를 VO로 변환하고 ID와 이메일 정보를 설정합니다.
		AssetStatusVo assetStatusVo = requestDto.toVo();
		assetStatusVo.setAssetId(assetId);
		assetStatusVo.setEmail(email);

		// DB 업데이트를 시도하고, 업데이트된 행이 없으면 예외를 발생시킵니다.
		if (assetStatusMapper.updateAssetStatus(assetStatusVo) == 0) {
			throw new AssetNotFoundException("ID가 " + assetId + "인 자산을 찾을 수 없거나 수정할 권한이 없습니다.");
		}

		// 자산 정보 변경에 따라 사용자 자산 요약 정보를 업데이트합니다.
		updateUserAssetSummary(email);
	}

	/**
	 * 특정 자산을 삭제합니다.
	 *
	 * @param assetId 삭제할 자산의 ID
	 * @param email 자산 소유자의 이메일
	 */
	public void deleteAssetStatus(Integer assetId, String email) {
		log.debug("Deleting asset. assetId: {}, email: {}", assetId, email);

		// DB 삭제를 시도하고, 삭제된 행이 없으면 예외를 발생시킵니다.
		int deleteResult = assetStatusMapper.deleteAssetStatus(assetId, email);
		log.debug("Deletion result count: {}", deleteResult);

		if (deleteResult == 0) {
			throw new NoSuchElementException("해당 자산이 사용자 계정에 존재하지 않습니다.");
		}

		// 자산 정보 변경에 따라 사용자 자산 요약 정보를 업데이트합니다.
		updateUserAssetSummary(email);
	}

	/**
	 * 특정 사용자의 모든 자산 목록을 조회합니다.
	 * 이 메서드는 다른 서비스에서 원본 VO 객체 리스트가 필요할 때 사용됩니다.
	 *
	 * @param email 사용자 이메일
	 * @return AssetStatusVo 객체 리스트
	 */
	@Override
	public List<AssetStatusVo> getFullAssetStatusByEmail(String email) {
		// 매퍼에 이미 존재하는 findAssetStatusByEmail 메소드를 그대로 호출합니다.
		return assetStatusMapper.findAssetStatusByEmail(email);
	}
}
