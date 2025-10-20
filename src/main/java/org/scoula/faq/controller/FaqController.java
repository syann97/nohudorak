package org.scoula.faq.controller;

import java.util.List;
import java.util.Map;

import org.scoula.faq.dto.FaqDto;
import org.scoula.faq.dto.FaqListDto;
import org.scoula.faq.service.FaqService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;

@Api(tags = "FAQ API", description = "자주 묻는 질문(FAQ) 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/faq")
public class FaqController {

	private final FaqService faqService;

	@ApiOperation(value = "FAQ 목록 조회", notes = "FAQ 목록(질문, 카테고리 등)을 조회합니다. (내용 제외)")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = FaqListDto.class, responseContainer = "List")
	})
	@GetMapping("/list")
	public ResponseEntity<List<FaqListDto>> faqListPage() {
		List<FaqListDto> faqList = faqService.getFaqList();
		return ResponseEntity.ok(faqList);
	}

	@ApiOperation(value = "FAQ 전체 상세 정보 조회", notes = "모든 FAQ의 상세 정보(내용 포함)를 한 번에 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = FaqDto.class, responseContainer = "List")
	})
	@GetMapping("/all")
	public ResponseEntity<List<FaqDto>> faqAllDetails() {
		List<FaqDto> faqsWithContent = faqService.getAllFaqsWithContent();
		return ResponseEntity.ok(faqsWithContent);
	}

	@ApiOperation(value = "특정 FAQ 상세 조회", notes = "ID로 특정 FAQ의 상세 정보를 조회합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "조회 성공", response = FaqDto.class),
		@ApiResponse(code = 404, message = "해당 ID의 FAQ를 찾을 수 없음")
	})
	@GetMapping("/{faqId}")
	public ResponseEntity<FaqDto> getFaqById(
		@ApiParam(value = "FAQ ID", required = true, example = "1")
		@PathVariable Integer faqId) {
		return ResponseEntity.ok(faqService.getFaqById(faqId));
	}
}
