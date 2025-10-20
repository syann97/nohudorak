package org.scoula.retirement.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.scoula.asset.dto.AssetStatusSummaryDto;
import org.scoula.asset.service.AssetStatusService;
import org.scoula.news.service.NewsService;
import org.scoula.product.domain.ProductVo;
import org.scoula.product.dto.FundDailyReturnDto;
import org.scoula.product.dto.ProductDto;
import org.scoula.product.mapper.ProductMapper;
import org.scoula.product.service.ProductService;
import org.scoula.recommend.service.CustomRecommendService;
import org.scoula.retirement.dto.RetirementMainResponseDto;
import org.scoula.user.dto.UserDto;
import org.scoula.user.dto.UserGraphDto;
import org.scoula.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "노후 관리 페이지 API", description = "노후 메인 페이지 데이터 조회 및 상품 상세 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/retirement")
public class RetirementController {

	private final UserService userService;
	private final AssetStatusService assetStatusService;
	private final ProductService productService;
	private final CustomRecommendService customRecommendService;
	private final ProductMapper productMapper;
	private final NewsService newsService;

	@ApiOperation(value = "노후 메인 페이지 데이터 조회", notes = "현재 로그인한 사용자의 노후 메인 페이지에 필요한 모든 데이터를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@GetMapping("")
	public ResponseEntity<RetirementMainResponseDto> getRetirementMainData(Authentication authentication) {
		String email = authentication.getName();

		// 0. 사용자 정보 조회
		UserDto userDto = userService.getUser(email);

		// 1. 자산 현황 데이터 조회
		List<AssetStatusSummaryDto> assetList = assetStatusService.getAssetStatusSummaryByEmail(email);

		// 1-2. DTO 구조에 맞춰 UserGraphDto 생성
		UserGraphDto userGraphDto = UserGraphDto.builder()
			.userName(userDto.getUserName())
			.assetStatus(assetList)
			.build();

		// 2. 나머지 데이터 조회 및 설정
		Map<String, List<? extends ProductDto>> allProducts = productService.findAllProducts();

		// 응답 DTO 생성
		RetirementMainResponseDto response = RetirementMainResponseDto.builder()
			.userInfo(userGraphDto)
			.allProducts(allProducts)
			.customRecommendPrdt(customRecommendService.getCustomRecommendsByEmail(email))
			.news(newsService.getAllNews())
			.build();

		return ResponseEntity.ok(response);
	}

	@ApiOperation(value = "금융 상품 상세 조회", notes = "금융 상품 코드로 특정 상품의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 상품 코드"),
		@ApiResponse(code = 400, message = "유효하지 않은 상품 카테고리")
	})
	@GetMapping("/{finPrdtCd}")
	public ResponseEntity<?> getProductDetail(@PathVariable String finPrdtCd,Authentication authentication) {
		String email = authentication.getName();

		ProductVo productVo = productService.getProductDetail(finPrdtCd);
		Map<String, Object> response = new HashMap<>();

		double userTendency = userService.getUser(email).getTendency();
		double userAssetProportion = userService.getUser(email).getAssetProportion();

		double productTendency = productVo.getTendency();
		double productAssetProportion = productVo.getAssetProportion();

		double rec = cosineSimilarity(userTendency,userAssetProportion,productTendency,productAssetProportion);

		response.put("rec",rec);

		response.put("product",productVo);
		//펀드 상품일 경우 응답 형식에 3개월 수익률도 추가
		if (finPrdtCd.matches("^[123].*")) { // startsWith 3번 대신 정규식
			List<FundDailyReturnDto> fundDailyReturnDtos =
				productService.getFundDailyReturnByCode(finPrdtCd)
					.stream()
					.map(FundDailyReturnDto::of) // 여기서 바로 변환
					.collect(Collectors.toList());
			response.put("fundReturn",fundDailyReturnDtos);
		}
		return ResponseEntity.ok(response);
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
}
