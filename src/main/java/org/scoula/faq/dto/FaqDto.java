package org.scoula.faq.dto;

import org.scoula.faq.domain.FaqVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "FAQ 상세 정보 DTO", description = "FAQ의 모든 정보를 담고 있는 데이터 전송 객체")
public class FaqDto {

	@ApiModelProperty(value = "FAQ ID", required = true, example = "1")
	private int faqId;

	@ApiModelProperty(value = "카테고리", required = true, example = "이용문의")
	private String category;

	@ApiModelProperty(value = "질문 제목", required = true, example = "로그인이 안 돼요.")
	private String title;

	@ApiModelProperty(value = "답변 내용", required = true, example = "비밀번호를 다시 확인해주세요.")
	private String content;

	/**
	 * FaqVo 객체를 FaqDto로 변환합니다.
	 * @param faq FaqVo 객체
	 * @return 변환된 FaqDto 객체
	 */
	public static FaqDto from(FaqVo faq) {
		return FaqDto.builder()
			.faqId(faq.getFaqId())
			.category(faq.getCategory())
			.title(faq.getTitle())
			.content(faq.getContent())
			.build();
	}

	/**
	 * FaqDto 객체를 FaqVo로 변환합니다.
	 * @return 변환된 FaqVo 객체
	 */
	public FaqVo toVo() {
		return FaqVo.builder()
			.faqId(faqId)
			.category(category)
			.title(title)
			.content(content)
			.build();
	}
}