package org.scoula.news.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "뉴스 DTO", description = "뉴스 목록 API 응답에서 사용되는 뉴스 단일 객체")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsDto {

	@ApiModelProperty(value = "뉴스 카테고리 번호", example = "1")
	private Integer category;

	@ApiModelProperty(value = "뉴스 제목", example = "오늘의 금융 뉴스")
	private String title;

	@ApiModelProperty(value = "뉴스 링크 URL", example = "https://news.example.com/article/1")
	private String link;

	@ApiModelProperty(value = "뉴스 발행 날짜", example = "2025-08-20")
	private String date;

	@ApiModelProperty(value = "뉴스 요약 내용", example = "오늘 금융 시장 동향 요약...")
	private String summary;
}
