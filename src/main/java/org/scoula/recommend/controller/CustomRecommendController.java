package org.scoula.recommend.controller;

import java.util.List;

import org.scoula.recommend.dto.CustomRecommendDto;
import org.scoula.recommend.service.CustomRecommendService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "맞춤 상품 추천 API", description = "사용자별 맞춤 금융 상품 추천 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/custom-recommends")
public class CustomRecommendController {

	private final CustomRecommendService customRecommendService;

	@ApiOperation(value = "내 맞춤 추천 상품 조회", notes = "현재 로그인한 사용자의 맞춤 추천 상품 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@GetMapping("/me") // 경로를 '나'를 의미하는 /me로 변경하여 보안 강화
	public ResponseEntity<List<CustomRecommendDto>> getMyCustomRecommends(Authentication authentication) {
		String email = authentication.getName(); // 인증 정보에서 이메일 추출
		return ResponseEntity.ok(customRecommendService.getCustomRecommendsByEmail(email));
	}
}
