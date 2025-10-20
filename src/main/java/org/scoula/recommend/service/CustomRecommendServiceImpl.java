package org.scoula.recommend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.scoula.product.domain.ProductVo;
import org.scoula.product.mapper.ProductMapper;
import org.scoula.product.service.ProductService;
import org.scoula.recommend.domain.CustomRecommendVo;
import org.scoula.recommend.dto.CustomRecommendDto;
import org.scoula.recommend.mapper.CustomRecommendMapper;
import org.scoula.user.dto.UserDto;
import org.scoula.user.service.UserService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomRecommendServiceImpl implements CustomRecommendService {

	private final CustomRecommendMapper customRecommendMapper;
	private final UserService userService;
	private final ProductService productService;

	@Override
	public List<CustomRecommendDto> getCustomRecommendsByEmail(String email) {
		List<CustomRecommendVo> recommendList = customRecommendMapper.getCustomRecommendsByEmail(email);

		// 추천 목록이 없는 초기 사용자를 위해 임시 데이터를 반환하는 로직
		if (recommendList == null || recommendList.isEmpty()) {
			return createTemporaryRecommendData();
		}

		return recommendList.stream()
			.map(CustomRecommendDto::of)
			.collect(Collectors.toList());
	}

	/**
	 * 사용자의 성향/자산 정보를 기반으로 모든 상품과의 유사도를 계산하여
	 * 새로운 맞춤 추천 상품 목록을 생성하고 DB에 저장합니다.
	 * @param email 추천 목록을 생성할 사용자의 이메일
	 */

	private final ProductMapper productMapper;

	@Override
	public void addCustomRecommend(String email) {

		// 1. 추천 계산에 필요한 사용자 정보와 전체 상품 목록을 가져옵니다.
		UserDto user = userService.getUser(email);
		List<? extends ProductVo> products = productMapper.findAllProduct();
		// List<? extends ProductVo> products = productService.findAllProducts();
		// List<? extends ProductVo> prdtList = productMapper.findAllProduct();

		// 2. 필수 정보가 없으면 로직을 중단합니다.
		if (user == null || products == null || products.isEmpty()) {
			return;
		}

		// 3. 최신 추천 목록을 생성하기 위해 기존 목록을 모두 삭제합니다.
		customRecommendMapper.deleteAllProductsByEmail(email);

		List<CustomRecommendVo> recommendVoList = new ArrayList<>();

		// 4. Null-safe하게 사용자 성향/자산 비율을 가져옵니다.
		double userTendency = (user.getTendency() != null) ? user.getTendency() : 0.0;
		double userAssetProportion = (user.getAssetProportion() != null) ? user.getAssetProportion() : 0.0;

		// 5. 모든 상품을 순회하며 코사인 유사도 점수를 계산합니다.
		for (ProductVo vo : products) {
			if (vo == null || vo.getFinPrdtCd() == null) {
				continue; // 필수 정보가 없는 상품은 건너뜁니다.
			}
			// Null-safe하게 상품 성향/자산 비율을 가져옵니다.
			double productTendency = (vo.getTendency() != null) ? vo.getTendency() : 0.0;
			double productAssetProportion = (vo.getAssetProportion() != null) ? vo.getAssetProportion() : 0.0;

			double score = cosineSimilarity(userTendency, userAssetProportion, productTendency, productAssetProportion);

			recommendVoList.add(new CustomRecommendVo(email, vo.getFinPrdtCd(), String.valueOf(score)));
		}

		// 6. 계산된 유사도 점수를 기준으로 내림차순 정렬합니다.
		recommendVoList.sort((a, b) -> {
			try {
				return Double.compare(Double.parseDouble(b.getScore()), Double.parseDouble(a.getScore()));
			} catch (NumberFormatException e) {
				return 0; // 점수 변환 실패 시 순서 변경 안함
			}
		});

		// 7. 상위 8개의 추천 상품만 DB에 저장합니다.
		for (int i = 0; i < Math.min(8, recommendVoList.size()); i++) {
			customRecommendMapper.insertCustomRecommend(recommendVoList.get(i));
		}
	}

	/**
	 * 두 벡터(사용자, 상품) 간의 코사인 유사도를 계산합니다.
	 * @param t1 사용자 성향
	 * @param a1 사용자 자산 비율
	 * @param t2 상품 성향
	 * @param a2 상품 자산 비율
	 * @return 0과 1 사이의 유사도 점수
	 */
	private double cosineSimilarity(double t1, double a1, double t2, double a2) {
		double dotProduct = t1 * t2 + a1 * a2;
		double normA = Math.sqrt(t1 * t1 + a1 * a1);
		double normB = Math.sqrt(t2 * t2 + a2 * a2);
		// 분모가 0이 되는 경우를 방지
		return (normA == 0 || normB == 0) ? 0 : dotProduct / (normA * normB);
	}

	/**
	 * 추천 데이터가 없는 사용자를 위한 임시 추천 데이터를 생성합니다.
	 * @return 임시 추천 DTO 리스트
	 */
	private List<CustomRecommendDto> createTemporaryRecommendData() {
		CustomRecommendDto temp1 = CustomRecommendDto.builder().finPrdtCd("TEMP001").score("85").build();
		CustomRecommendDto temp2 = CustomRecommendDto.builder().finPrdtCd("TEMP002").score("90").build();
		return List.of(temp1, temp2);
	}
}
