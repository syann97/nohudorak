package org.scoula.faq.dto;

import org.scoula.faq.domain.FaqVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "FAQ 목록 DTO", description = "FAQ 목록 조회를 위한 요약 정보(ID, 카테고리, 제목)를 담은 DTO")
public class FaqListDto {

	@ApiModelProperty(value = "FAQ ID", required = true, example = "1")
	private int faqId;

	@ApiModelProperty(value = "카테고리", required = true, example = "이용문의")
	private String category;

	@ApiModelProperty(value = "질문 제목", required = true, example = "로그인이 안 돼요.")
	private String title;

	/**
	 * FaqVo 객체를 FaqListDto로 변환합니다.
	 * @param faq FaqVo 객체
	 * @return 변환된 FaqListDto 객체
	 */
	public static FaqListDto from(FaqVo faq) {
		return FaqListDto.builder()
			.faqId(faq.getFaqId())
			.category(faq.getCategory())
			.title(faq.getTitle())
			.build();
	}
}