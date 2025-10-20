package org.scoula.View.home.controller;

import org.scoula.View.home.dto.HomeResponseDto;
import org.scoula.View.home.service.HomeService;
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

/**
 * 애플리케이션의 홈 화면에 필요한 데이터를 제공하는 컨트롤러
 */
@Api(tags = "홈 페이지 API", description = "메인 홈 화면 데이터 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
public class HomeController {

	private final HomeService homeService;

	@ApiOperation(value = "홈 화면 데이터 조회", notes = "로그인 상태에 따라 맞춤형 홈 화면 데이터를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공")
	})
	@GetMapping()
	public ResponseEntity<HomeResponseDto> getHome(Authentication authentication) {
		// 인증 정보가 없거나, 인증되지 않았거나, 익명 사용자인 경우 비로그인 상태로 처리
		if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal()
			.equals("anonymousUser")) {
			return ResponseEntity.ok(homeService.getHomeData(null)); // 비로그인 대응
		}

		String email = authentication.getName();
		return ResponseEntity.ok(homeService.getHomeData(email));
	}
  
	@ApiOperation(value = "테스트 엔드포인트", notes = "배포 테스트용 간단한 GET 엔드포인트입니다.")
	@GetMapping("/test")
	public ResponseEntity<String> test(){
		return ResponseEntity.ok("Hello from JejuGom Server! Deployment test successful.");
	}
}
