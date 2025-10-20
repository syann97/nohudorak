package org.scoula.branch.controller;

import java.util.List;

import org.scoula.branch.dto.BranchDto;
import org.scoula.branch.service.BranchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "지점 API", description = "은행 지점 정보 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/branches")
public class BranchController {

	private final BranchService branchService;

	@ApiOperation(value = "모든 지점 목록 조회", notes = "시스템에 등록된 모든 지점의 목록을 조회합니다.")
	@ApiResponse(code = 200, message = "조회 성공")
	@GetMapping
	public ResponseEntity<List<BranchDto>> getAllBranches() {
		return ResponseEntity.ok(branchService.getAllBranches());
	}

	@ApiOperation(value = "특정 지점 조회", notes = "지점 ID로 특정 지점의 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 지점")
	})
	@GetMapping("/{branchId}")
	public ResponseEntity<BranchDto> getBranchById(
		@ApiParam(value = "조회할 지점의 ID", required = true, example = "101")
		@PathVariable Integer branchId) {
		return ResponseEntity.ok(branchService.getBranchById(branchId));
	}
}