package org.scoula.gift.controller;

import org.scoula.exception.ErrorResponse;
import org.scoula.gift.dto.GiftPageResponseDto;
import org.scoula.gift.dto.RecipientIdResponseDto;
import org.scoula.gift.dto.RecipientRequestDto;
import org.scoula.gift.dto.RecipientResponseDto;
import org.scoula.gift.service.RecipientService;
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

@Api(tags = "수증자 API", description = "증여 시뮬레이션 수증자 관련 API")
@RestController
@RequestMapping("/api/gift")
public class RecipientController {

	private final RecipientService recipientService;

	public RecipientController(RecipientService recipientService) {
		this.recipientService = recipientService;
	}

	@ApiOperation(value = "수증자 정보 생성", notes = "새로운 수증자 정보를 등록합니다.")
	@ApiResponses({
		@ApiResponse(code = 201, message = "생성 성공", response = RecipientIdResponseDto.class),
		@ApiResponse(code = 400, message = "잘못된 요청 형식", response = ErrorResponse.class),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@PostMapping
	public ResponseEntity<RecipientIdResponseDto> createRecipient(
		@ApiParam(value = "수증자 생성 정보", required = true) @RequestBody RecipientRequestDto requestDto,
		Authentication authentication) {

		String email = authentication.getName();
		RecipientResponseDto fullResponseDto = recipientService.createRecipient(requestDto, email);
		RecipientIdResponseDto idResponse = new RecipientIdResponseDto(fullResponseDto.getRecipientId());
		return ResponseEntity.status(HttpStatus.CREATED).body(idResponse);
	}

	@ApiOperation(value = "증여 페이지 데이터 조회", notes = "로그인한 사용자의 전체 수증자 목록과 자산 요약 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = GiftPageResponseDto.class),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@GetMapping
	public ResponseEntity<GiftPageResponseDto> getRecipients(Authentication authentication) {
		String email = authentication.getName();
		GiftPageResponseDto responseDto = recipientService.getGiftPageData(email);
		return ResponseEntity.ok(responseDto);
	}

	@ApiOperation(value = "특정 수증자 정보 상세 조회", notes = "수증자 ID로 특정 수증자의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = RecipientResponseDto.class),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 404, message = "존재하지 않는 수증자 또는 접근 권한 없음"),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@GetMapping("/{recipientId}")
	public ResponseEntity<RecipientResponseDto> getRecipient(
		@ApiParam(value = "수증자 ID", required = true, example = "1") @PathVariable Integer recipientId,
		Authentication authentication) {

		String email = authentication.getName();
		RecipientResponseDto responseDto = recipientService.findRecipientByIdAndEmail(recipientId, email);
		if (responseDto != null) {
			return ResponseEntity.ok(responseDto);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@ApiOperation(value = "수증자 정보 수정", notes = "특정 수증자의 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "수정 성공 (No Content)"),
		@ApiResponse(code = 400, message = "잘못된 요청 형식", response = ErrorResponse.class),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 404, message = "존재하지 않는 수증자 또는 접근 권한 없음"),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@PutMapping("/{recipientId}")
	public ResponseEntity<Void> updateRecipient( // ### 반환 타입을 ResponseEntity<Void>로 수정하여 오류 해결 ###
		@ApiParam(value = "수증자 ID", required = true, example = "1") @PathVariable Integer recipientId,
		@ApiParam(value = "수증자 수정 정보", required = true) @RequestBody RecipientRequestDto requestDto,
		Authentication authentication) {

		String email = authentication.getName();
		RecipientResponseDto updatedDto = recipientService.updateRecipient(recipientId, requestDto, email);
		if (updatedDto != null) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		}
	}

	@ApiOperation(value = "수증자 정보 삭제", notes = "특정 수증자의 정보를 삭제합니다.")
	@ApiResponses({
		@ApiResponse(code = 204, message = "삭제 성공 (No Content)"),
		@ApiResponse(code = 401, message = "인증 실패"),
		@ApiResponse(code = 404, message = "존재하지 않는 수증자 또는 접근 권한 없음"),
		@ApiResponse(code = 500, message = "서버 내부 오류", response = ErrorResponse.class)
	})
	@DeleteMapping("/{recipientId}")
	public ResponseEntity<Void> deleteRecipient(
		@ApiParam(value = "수증자 ID", required = true, example = "1") @PathVariable Integer recipientId,
		Authentication authentication) {

		String email = authentication.getName();
		boolean success = recipientService.deleteRecipient(recipientId, email);
		if (success) {
			return ResponseEntity.noContent().build();
		} else {
			return ResponseEntity.notFound().build();
		}
	}
}
