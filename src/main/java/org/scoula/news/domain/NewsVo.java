package org.scoula.news.domain;

import java.time.LocalDateTime;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "뉴스 정보", description = "뉴스 목록 조회 시 반환되는 뉴스 단일 객체")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class NewsVo {

	@ApiModelProperty(value = "뉴스 ID", example = "1")
	private Long id;

	@ApiModelProperty(value = "카테고리 번호", example = "2")
	private Integer category;

	@ApiModelProperty(value = "뉴스 제목", example = "오늘의 금융 뉴스")
	private String title;

	@ApiModelProperty(value = "뉴스 링크 URL", example = "https://news.example.com/article/1")
	private String link;

	@ApiModelProperty(value = "뉴스 발행 날짜", example = "2025-08-20")
	private String date;

	@ApiModelProperty(value = "뉴스 요약 내용", example = "오늘 금융 시장 동향...")
	private String summary;

	@ApiModelProperty(value = "DB 생성 시각", example = "2025-08-20T12:34:56")
	private LocalDateTime createdAt;
}
