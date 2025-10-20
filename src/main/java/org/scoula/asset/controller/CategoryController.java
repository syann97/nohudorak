package org.scoula.asset.controller;

import java.util.List;

import org.scoula.asset.dto.CategoryDto;
import org.scoula.asset.service.CategoryService;
import org.springframework.http.ResponseEntity;
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

@Api(tags = "자산 카테고리 API", description = "자산 카테고리 관리 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

	private final CategoryService categoryService;

	@ApiOperation(value = "모든 카테고리 조회", notes = "시스템에 등록된 모든 자산 카테고리 목록을 조회합니다.")
	@ApiResponse(code = 200, message = "조회 성공")
	@GetMapping
	public ResponseEntity<List<CategoryDto>> getAllCategories() {
		return ResponseEntity.ok(categoryService.getAllCategories());
	}

	@ApiOperation(value = "특정 카테고리 조회", notes = "카테고리 코드로 특정 자산 카테고리의 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 카테고리")
	})
	@GetMapping("/{assetCategoryCode}")
	public ResponseEntity<CategoryDto> getCategoryByCode(
		@ApiParam(value = "조회할 카테고리 코드", required = true, example = "01")
		@PathVariable String assetCategoryCode) {
		return ResponseEntity.ok(categoryService.getCategoryByCode(assetCategoryCode));
	}

	@ApiOperation(value = "신규 카테고리 추가", notes = "새로운 자산 카테고리를 시스템에 추가합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "추가 성공"),
		@ApiResponse(code = 400, message = "잘못된 요청 데이터")
	})
	@PostMapping
	public ResponseEntity<Void> addCategory(@RequestBody CategoryDto categoryDto) {
		categoryService.addCategory(categoryDto);
		return ResponseEntity.ok().build();
	}

	@ApiOperation(value = "카테고리 정보 수정", notes = "기존 자산 카테고리의 정보를 수정합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "수정 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 카테고리")
	})
	@PutMapping("/{assetCategoryCode}")
	public ResponseEntity<Void> updateCategory(
		@ApiParam(value = "수정할 카테고리 코드", required = true, example = "01")
		@PathVariable String assetCategoryCode,
		@RequestBody CategoryDto categoryDto) {
		categoryDto.setAssetCategoryCode(assetCategoryCode);
		categoryService.updateCategory(categoryDto);
		return ResponseEntity.ok().build();
	}

	@ApiOperation(value = "카테고리 삭제", notes = "특정 자산 카테고리를 시스템에서 삭제합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "삭제 성공"),
		@ApiResponse(code = 404, message = "존재하지 않는 카테고리")
	})
	@DeleteMapping("/{assetCategoryCode}")
	public ResponseEntity<Void> deleteCategory(
		@ApiParam(value = "삭제할 카테고리 코드", required = true, example = "01")
		@PathVariable String assetCategoryCode) {
		categoryService.deleteCategory(assetCategoryCode);
		return ResponseEntity.ok().build();
	}
}