package org.scoula.View.codef.controller;

import java.util.Map;

import org.scoula.View.codef.dto.ConnectedIdRequestDto;
import org.scoula.View.codef.service.CodefTokenService;
import org.scoula.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Api(tags = "자산 연동 (CODEF) API", description = "CODEF API를 이용한 계정 연결 및 자산 정보 조회")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/codef")
@Log4j2
public class CodefController {

	private final CodefTokenService codefTokenService;
	private final UserService userService;

	@ApiOperation(value = "CODEF Access Token 조회", notes = "서버가 현재 보유하고 있는 CODEF Access Token을 조회합니다. (주로 개발 및 디버깅용)")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = String.class),
		@ApiResponse(code = 500, message = "토큰 조회 실패")
	})
	@GetMapping("/access-token")
	public ResponseEntity<String> getAccessToken() {
		String accessToken = codefTokenService.getAccessToken();
		if (accessToken != null)
			return ResponseEntity.ok(accessToken);
		else
			return ResponseEntity.status(500).body("Failed to get access token.");
	}

	@ApiOperation(value = "계정 연결(ConnectedId) 생성", notes = "사용자의 금융기관 정보를 CODEF에 등록하고, 반환된 connectedId를 서버에 저장합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "ConnectedId 생성 및 저장 성공", response = Map.class),
		@ApiResponse(code = 400, message = "잘못된 요청 (계좌 정보 누락 또는 은행 아이디/비밀번호 오류)"),
		@ApiResponse(code = 500, message = "서버 오류 (CODEF 응답 오류 등)")
	})
	@PostMapping("/connected-id")
	public ResponseEntity<?> createConnectedId(@RequestBody ConnectedIdRequestDto requestDto,
		Authentication authentication) {
		log.info("📩 ConnectedId 생성 요청: {}", requestDto);

		if (requestDto.getAccountList() == null || requestDto.getAccountList().isEmpty()) {
			log.error("❌ accountList 누락");
			throw new IllegalArgumentException("고객의 은행 정보를 입력해주세요.");
		}

		Map<String, Object> result = codefTokenService.createConnectedId(requestDto);
		if (result == null) {
			log.error("❌ CODEF 응답이 null");
			throw new RuntimeException("CODEF에서 응답을 받아오지 못했습니다.");
		}

		Map<String, Object> resultInfo = (Map<String, Object>)result.get("result");
		String code = (String)resultInfo.get("code");
		if (!"CF-00000".equals(code)) {
			log.error("❌ CODEF 실패 응답 코드: {}", code);
			if ("CF-04000".equals(code)) {
				throw new IllegalArgumentException("은행 아이디/비밀번호 오류");
			} else {
				throw new RuntimeException("CODEF 응답 오류 : " + code);
			}
		}

		Map<String, Object> data = (Map<String, Object>)result.get("data");
		String connectedId = (String)data.get("connectedId");

		String userEmail = authentication.getName();

		if (userEmail != null && connectedId != null) {
			log.info("✅ ConnectedId 저장 - userEmail: {}, connectedId: {}", userEmail, connectedId);
			userService.updateConnectedId(userEmail, connectedId);
			codefTokenService.saveAccountInfo(userEmail, connectedId);
			return ResponseEntity.ok(200);
		} else {
			log.error("❌ userEmail 또는 connectedId null - {}, {}", userEmail, connectedId);
			log.info(connectedId);
			log.info(result);
			throw new RuntimeException("서버 에러 : CODEF 에서 connectedId를 받아오지 못했거나 서버에서 유저의 email을 받아오지 못했습니다. " + code);
		}
	}
}
