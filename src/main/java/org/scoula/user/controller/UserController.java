package org.scoula.user.controller;

import org.scoula.user.dto.MyPageResponseDto;
import org.scoula.user.dto.UserBranchIdDto;
import org.scoula.user.dto.UserBranchNameDto;
import org.scoula.user.dto.UserDto;
import org.scoula.user.dto.UserInfoResponseDto;
import org.scoula.user.dto.UserInfoUpdateRequestDto;
import org.scoula.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@Api(tags = "사용자 API", description = "사용자 정보 조회, 가입, 수정, 탈퇴 등 관련 API")
public class UserController {

	private final UserService userService;

	@ApiOperation(value = "회원 가입", notes = "새로운 사용자를 등록합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "회원 가입 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청 데이터")
	})
	@PostMapping("/join")
	public ResponseEntity<Void> join(@RequestBody UserDto userDto) {
		userService.join(userDto);
		return ResponseEntity.ok().build();
	}

	@ApiOperation(value = "특정 회원 정보 조회 (관리자용)", notes = "이메일을 기반으로 특정 사용자 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 404, message = "사용자를 찾을 수 없음")
	})
	@GetMapping("/{email}")
	public ResponseEntity<UserDto> getUser(
		@ApiParam(value = "조회할 사용자의 이메일", required = true) @PathVariable String email) {
		UserDto user = userService.getUser(email);
		return ResponseEntity.ok(user);
	}

	@ApiOperation(value = "내 기본 정보 조회", notes = "현재 로그인한 사용자의 이메일, 이름, 전화번호, 생년월일을 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "사용자를 찾을 수 없음")
	})
	@GetMapping("/me")
	public ResponseEntity<UserInfoResponseDto> getMyInfo(Authentication authentication) {
		String email = authentication.getName();
		UserInfoResponseDto userInfo = userService.getUserInfo(email);
		return ResponseEntity.ok(userInfo);
	}

	@ApiOperation(value = "내 정보 수정", notes = "현재 로그인한 사용자의 이름, 전화번호, 생년월일을 수정합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "수정 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "사용자를 찾을 수 없음")
	})
	@PatchMapping("/me") // '나'의 정보를 수정하므로 /me 엔드포인트와 PATCH 메소드 사용
	public ResponseEntity<Void> updateMyInfo(
		Authentication authentication,
		@RequestBody UserInfoUpdateRequestDto requestDto) {

		String email = authentication.getName(); // Spring Security를 통해 현재 사용자 이메일 획득
		userService.updateUserInfo(email, requestDto);
		return ResponseEntity.noContent().build(); // 성공 시 204 No Content 응답
	}

	/**
	 * 내 지점 정보 조회 컨트롤러
	 */
	@ApiOperation(value = "내 지점 정보 조회", notes = "현재 로그인한 사용자의 선호 지점 이름을 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = UserBranchNameDto.class), // response 클래스 변경
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "사용자 또는 지점 정보 없음")
	})
	@GetMapping("/branch")
	public ResponseEntity<UserBranchNameDto> getMyBranchInfo(Authentication authentication) {
		String email = authentication.getName();
		UserBranchNameDto branchNameDto = userService.getBranchInfo(email);
		return ResponseEntity.ok(branchNameDto);
	}

	@ApiOperation(value = "내 지점 ID 수정", notes = "현재 로그인한 사용자의 선호 지점 ID를 수정합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "수정 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@PatchMapping("/branch") // '나'의 정보를 수정하는 엔드포인트
	public ResponseEntity<Void> updateMyBranchId(
		Authentication authentication,
		@RequestBody UserBranchIdDto requestDto) {

		String email = authentication.getName();
		userService.updateBranchId(email, requestDto.getBranchId());
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "회원 탈퇴", notes = "현재 로그인한 사용자의 계정을 탈퇴 처리합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "회원 탈퇴 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "사용자를 찾을 수 없음")
	})
	@DeleteMapping("/me") // '나'의 계정을 삭제하는 엔드포인트
	public ResponseEntity<Void> withdrawUser(Authentication authentication) {
		String email = authentication.getName();

		userService.withdrawUser(email);

		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "마이페이지 정보 조회", notes = "현재 로그인한 사용자의 마이페이지 데이터를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@GetMapping("/mypage")
	public ResponseEntity<MyPageResponseDto> getMyPageInfo(Authentication authentication) {
		String email = authentication.getName();
		MyPageResponseDto myPageData = userService.getMyPageData(email);
		return ResponseEntity.ok(myPageData);
	}
}
