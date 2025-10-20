package org.scoula.faq.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * FAQ 데이터베이스 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqVo {
	/** FAQ 고유 ID */
	private int faqId;

	/** FAQ 카테고리 (예: 이용문의, 계정) */
	private String category;

	/** 질문 제목 */
	private String title;

	/** 답변 내용 */
	private String content;
}