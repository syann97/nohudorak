package org.scoula.gift.service;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.scoula.asset.domain.AssetStatusVo;
import org.scoula.asset.service.AssetStatusService;
import org.scoula.gift.domain.RecipientVo;
import org.scoula.gift.dto.GiftAssetCategoryDto;
import org.scoula.gift.dto.GiftAssetDto;
import org.scoula.gift.dto.GiftPageResponseDto;
import org.scoula.gift.dto.RecipientRequestDto;
import org.scoula.gift.dto.RecipientResponseDto;
import org.scoula.gift.mapper.RecipientMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class RecipientServiceImpl implements RecipientService {

	private final RecipientMapper recipientMapper;
	private final AssetStatusService assetStatusService;

	/**
	 * 새로운 수증자 정보를 생성합니다.
	 *
	 * @param requestDto 생성할 수증자 정보가 담긴 DTO
	 * @param email      현재 인증된 사용자의 이메일
	 * @return 생성된 수증자 정보가 담긴 응답 DTO
	 * @throws IllegalStateException 데이터베이스에 정상적으로 생성된 후 조회가 실패하는 경우 발생
	 */
	@Override
	public RecipientResponseDto createRecipient(RecipientRequestDto requestDto, String email) {
		RecipientVo vo = requestDto.toVo(email);
		// email 정보가 필요하다면 vo에 설정하는 로직 추가
		// vo.setEmail(email);
		recipientMapper.insertRecipient(vo);

		// 생성 후 즉시 조회하여 데이터 무결성 확인
		RecipientVo createdVo = recipientMapper.findById(vo.getRecipientId());
		if (createdVo == null) {
			// 이 경우는 거의 없지만, 발생 시 서버 내부 문제임
			throw new IllegalStateException("수증자 정보 생성 후 데이터를 조회할 수 없습니다.");
		}

		// 포맷팅 로직이 포함된 from 메서드를 호출
		return RecipientResponseDto.from(createdVo);
	}

	/**
	 * 특정 수증자 정보를 조회합니다.
	 * 해당 수증자가 존재하지 않거나, 현재 사용자의 소유가 아닐 경우 예외를 발생시킵니다.
	 *
	 * @param recipientId 조회할 수증자의 ID
	 * @param email       현재 인증된 사용자의 이메일
	 * @return 조회된 수증자 정보가 담긴 응답 DTO
	 * @throws NoSuchElementException 요청한 ID의 수증자가 없거나 접근 권한이 없을 경우 발생
	 */
	@Override
	public RecipientResponseDto findRecipientByIdAndEmail(Integer recipientId, String email) {
		// 이 메서드는 DB에서 recipientId와 email을 모두 사용하여 데이터를 조회해야 합니다.
		RecipientVo vo = recipientMapper.findByIdAndEmail(recipientId, email);
		if (vo == null) {
			// null을 반환하는 대신, 예외를 던져 GlobalExceptionHandler가 404로 처리하도록 함
			throw new NoSuchElementException("ID " + recipientId + "에 해당하는 수증자를 찾을 수 없거나 접근 권한이 없습니다.");
		}
		return RecipientResponseDto.from(vo);
	}

	/**
	 * 특정 수증자 정보를 수정합니다.
	 * 수정 전, 해당 수증자가 존재하는지와 현재 사용자의 소유인지를 먼저 검증합니다.
	 *
	 * @param recipientId 수정할 수증자의 ID
	 * @param requestDto  수정할 내용이 담긴 DTO
	 * @param email       현재 인증된 사용자의 이메일
	 * @return 수정된 수증자 정보가 담긴 응답 DTO
	 * @throws NoSuchElementException 수정할 대상이 없거나 접근 권한이 없을 경우 발생
	 */
	@Override
	public RecipientResponseDto updateRecipient(Integer recipientId, RecipientRequestDto requestDto, String email) {
		// 1. 수정할 데이터가 존재하는지, 현재 사용자의 소유인지 확인 (없으면 여기서 예외 발생)
		findRecipientByIdAndEmail(recipientId, email);

		// 2. 수정할 내용으로 VO 객체 생성
		RecipientVo voToUpdate = requestDto.toVo(email);
		voToUpdate.setRecipientId(recipientId);

		// 3. DB 업데이트
		recipientMapper.updateRecipient(voToUpdate);

		// 4. 업데이트된 정보를 DB에서 다시 조회하여 완전한 객체를 얻음
		RecipientVo updatedVo = recipientMapper.findById(recipientId);
		if (updatedVo == null) {
			// 업데이트 직후 조회가 안되는 경우는 심각한 문제
			throw new IllegalStateException("수증자 정보 수정 후 데이터를 조회할 수 없습니다.");
		}

		// 5. 조회된 완전한 객체를 DTO로 변환하여 반환 (날짜 포맷팅 포함)
		return RecipientResponseDto.from(updatedVo);
	}

	/**
	 * 특정 수증자 정보를 삭제합니다.
	 * 삭제 전, 해당 수증자가 존재하는지와 현재 사용자의 소유인지를 먼저 검증합니다.
	 *
	 * @param recipientId 삭제할 수증자의 ID
	 * @param email       현재 인증된 사용자의 이메일
	 * @return 삭제 성공 여부 (true: 성공, false: 실패)
	 * @throws NoSuchElementException 삭제할 대상이 없거나 접근 권한이 없을 경우 발생
	 */
	@Override
	public boolean deleteRecipient(Integer recipientId, String email) {
		// 1. 삭제할 데이터가 존재하는지, 현재 사용자의 소유인지 확인 (없으면 여기서 예외 발생)
		findRecipientByIdAndEmail(recipientId, email);

		// 2. DB에서 삭제
		int affectedRows = recipientMapper.deleteById(recipientId);
		return affectedRows > 0;
	}

	/**
	 * 증여 페이지에 필요한 전체 데이터(수증자 목록, 카테고리별 상세 자산 목록)를 조회합니다.
	 *
	 * @param email 현재 인증된 사용자의 이메일
	 * @return 수증자 목록과 카테고리별 자산 정보가 담긴 최종 응답 DTO
	 */
	@Override
	public GiftPageResponseDto getGiftPageData(String email) {
		// 1. 수증자 목록을 조회하고 DTO 리스트로 변환합니다.
		List<RecipientVo> recipientVoList = recipientMapper.findByEmail(email);
		List<RecipientResponseDto> recipientDtoList = recipientVoList.stream()
			.map(RecipientResponseDto::from)
			.collect(Collectors.toList());

		// 2. DB에서 사용자의 '모든' 자산 목록(Vo)을 가져옵니다.
		List<AssetStatusVo> allAssets = assetStatusService.getFullAssetStatusByEmail(email);

		// 3. 카테고리 코드를 기준으로 자산들을 그룹화합니다.
		Map<String, List<AssetStatusVo>> groupedAssets = allAssets.stream()
			.collect(Collectors.groupingBy(AssetStatusVo::getAssetCategoryCode));

		// 4. 그룹화된 데이터를 최종 DTO 구조(List<GiftAssetCategoryDto>)로 변환합니다.
		List<GiftAssetCategoryDto> assetCategories = groupedAssets.entrySet().stream()
			.map(entry -> {
				String categoryCode = entry.getKey();
				List<AssetStatusVo> assetsInCategory = entry.getValue();

				// 4-1. 카테고리별 자산 총액을 계산합니다.
				long totalAmount = assetsInCategory.stream()
					.mapToLong(AssetStatusVo::getAmount)
					.sum();

				// 4-2. 개별 자산 목록을 GiftAssetDto 리스트로 변환합니다.
				List<GiftAssetDto> giftAssets = assetsInCategory.stream()
					.map(GiftAssetDto::of)
					.collect(Collectors.toList());

				// 4-3. 최종 DTO를 생성합니다. (categoryName 없이)
				return GiftAssetCategoryDto.builder()
					.assetCategoryCode(categoryCode)
					.totalAmount(totalAmount)
					.assets(giftAssets)
					.build();
			})
			.collect(Collectors.toList());

		// 5. 최종 응답 DTO를 빌드하여 반환합니다.
		return GiftPageResponseDto.builder()
			.recipients(recipientDtoList)
			.assetCategories(assetCategories)
			.build();
	}
}
