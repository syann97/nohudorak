package org.scoula.asset.controller;

import java.util.List;

import org.scoula.asset.dto.AssetStatusIdDto;
import org.scoula.asset.dto.AssetStatusRequestDto;
import org.scoula.asset.dto.AssetStatusResponseDto;
import org.scoula.asset.service.AssetStatusService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "사용자 자산 현황 API", description = "사용자 자산 현황을 조회, 추가, 수정, 삭제하는 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/assets")
public class AssetStatusController {

	private final AssetStatusService assetStatusService;

	// 사용자 이메일 조회를 위한 비공개 헬퍼 메소드
	private String getUserEmail(Authentication authentication) {
		// 인증 정보가 없을 경우를 대비한 방어 코드
		if (authentication == null || authentication.getName() == null) {
			throw new SecurityException("인증 정보가 없습니다.");
		}
		return authentication.getName();
	}

	@ApiOperation(value = "내 자산 현황 목록 조회", notes = "현재 로그인한 사용자의 모든 자산 현황 목록을 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@GetMapping()
	public ResponseEntity<List<AssetStatusResponseDto>> getAssetStatusByEmail(Authentication authentication) {
		String userEmail = getUserEmail(authentication);
		List<AssetStatusResponseDto> assetStatusResponseDtos = assetStatusService.getAssetStatusByEmail(userEmail);
		return ResponseEntity.ok(assetStatusResponseDtos);
	}

	@ApiOperation(value = "자산 추가", notes = "새로운 자산 정보를 추가합니다. 추가 후 사용자의 전체 자산 및 자산 비중이 재계산됩니다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "자산 추가 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청 데이터"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자")
	})
	@PostMapping
	public ResponseEntity<AssetStatusIdDto> addAssetStatus(
		@RequestBody AssetStatusRequestDto requestDto,
		Authentication authentication) {

		String userEmail = getUserEmail(authentication);
		AssetStatusIdDto responseDto = assetStatusService.addAssetStatus(userEmail, requestDto);

		// 리소스가 성공적으로 생성되었음을 의미하는 201 Created 상태와 함께
		// 생성된 ID를 응답 본문에 담아 반환합니다.
		return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
	}


	@ApiOperation(value = "자산 정보 수정", notes = "기존 자산 정보를 수정합니다. 수정 후 사용자의 전체 자산 및 자산 비중이 재계산됩니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "수정 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청 데이터"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "존재하지 않는 자산이거나 수정 권한 없음")
	})
	@PutMapping("/{assetId}")
	public ResponseEntity<Void> updateAssetStatus(
		@ApiParam(value = "수정할 자산의 ID", required = true, example = "1")
		@PathVariable Integer assetId,
		@RequestBody AssetStatusRequestDto requestDto,
		Authentication authentication) {

		String userEmail = getUserEmail(authentication);
		assetStatusService.updateAssetStatus(assetId, userEmail, requestDto);
		return ResponseEntity.noContent().build();
	}

	@ApiOperation(value = "자산 삭제", notes = "자산 정보를 삭제합니다. 삭제 후 사용자의 전체 자산 및 자산 비중이 재계산됩니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "삭제 성공"),
		@ApiResponse(code = 401, message = "인증되지 않은 사용자"),
		@ApiResponse(code = 404, message = "존재하지 않는 자산이거나 삭제 권한 없음")
	})
	@DeleteMapping("/{assetId}")
	public ResponseEntity<Void> deleteAssetStatus(
		@ApiParam(value = "삭제할 자산의 ID", required = true, example = "1")
		@PathVariable Integer assetId,
		Authentication authentication) {

		String email = getUserEmail(authentication);
		assetStatusService.deleteAssetStatus(assetId, email);
		return ResponseEntity.noContent().build();
	}
}
