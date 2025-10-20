package org.scoula.news.controller;

import java.util.List;

import org.scoula.news.dto.NewsDto;
import org.scoula.news.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = "뉴스 API", description = "뉴스 크롤링 및 조회 관련 API")
@RestController
@RequestMapping("/api/news")
public class NewsController {

	@Autowired
	private NewsService newsService;

	@ApiOperation(value = "뉴스 수동 크롤링", notes = "뉴스를 수동으로 크롤링하고 DB에 저장합니다. 변경된 카테고리 번호를 반환합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "크롤링 완료 및 변경된 카테고리 반환"),
		@ApiResponse(code = 500, message = "서버 내부 오류")
	})
	@PostMapping("/crawl")
	public ResponseEntity<String> crawlNewsManually() {
		List<Integer> updatedCategories = newsService.crawlAndSaveNews();
		if (updatedCategories.isEmpty()) {
			return ResponseEntity.ok("No new categories were updated.");
		}
		//  변경된 카테고리 번호만 반환
		return ResponseEntity.ok("Change category is: " + updatedCategories);
	}

	@ApiOperation(value = "뉴스 목록 조회", notes = "DB에 저장된 뉴스 전체 목록을 반환합니다.")
	@ApiResponses({
		@ApiResponse(code = 200, message = "뉴스 목록 반환", response = NewsDto.class, responseContainer = "List"),
		@ApiResponse(code = 500, message = "서버 내부 오류")
	})
	@GetMapping("")
	public ResponseEntity<List<NewsDto>> getNews() {
		return ResponseEntity.ok(newsService.getAllNews());
	}
}
