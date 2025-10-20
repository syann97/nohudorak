package org.scoula.faq.service;

import java.util.List;

import org.scoula.faq.dto.FaqDto;
import org.scoula.faq.dto.FaqListDto;

public interface FaqService {

	/**
	 * FAQ 목록을 조회합니다.
	 * 제목만 포함된 가벼운 목록을 반환하여 페이지 로딩을 최적화합니다.
	 * @return FAQ 제목 리스트
	 */
	List<FaqListDto> getFaqList();

	/**
	 * 모든 FAQ의 상세 내용(질문, 답변)을 조회합니다.
	 * 전체 데이터 로드가 필요한 기능에 사용됩니다.
	 * @return 질문과 답변을 포함한 FAQ 리스트
	 */
	List<FaqDto> getAllFaqsWithContent();

	/**
	 * 특정 ID의 FAQ 상세 내용을 조회합니다.
	 * @param faqId 조회할 FAQ의 고유 ID
	 * @return 해당 ID의 FAQ 상세 정보
	 */
	FaqDto getFaqById(Integer faqId);
}