package org.scoula.View.preference.controller;

import org.scoula.View.preference.dto.PreferenceRequestDto;
import org.scoula.View.preference.service.PreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

/**
 * 사용자 성향 설문 관련 API를 처리하는 컨트롤러
 */
@Api(tags = "사용자 성향 설문 API", description = "사용자 투자 성향 설문 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/preference")
public class PreferenceController {
	private final PreferenceService preferenceService;

	@ApiOperation(value = "사용자 성향 정보 제출", notes = "사용자가 제출한 설문 답변을 바탕으로 투자 성향을 계산하고 저장합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "성공적으로 처리됨"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@PostMapping()
	public ResponseEntity<Void> setUserPreference(@RequestBody PreferenceRequestDto requestDto,
		Authentication authentication) {
		String userName = authentication.getName(); // Spring Security 컨텍스트에서 사용자 이메일(ID)을 가져옴
		preferenceService.setUserPreference(requestDto, userName);
		return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
	}
}
