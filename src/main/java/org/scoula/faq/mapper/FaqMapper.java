package org.scoula.faq.mapper;

import java.util.List;
import org.scoula.faq.domain.FaqVo;

/**
 * FAQ 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
public interface FaqMapper {
	/**
	 * 모든 FAQ 목록을 조회합니다.
	 * @return FAQ 정보(FaqVo) 리스트
	 */
	List<FaqVo> getAllFaqs();

	/**
	 * FAQ ID로 특정 FAQ 정보를 조회합니다.
	 * @param id 조회할 FAQ의 ID
	 * @return 해당 FAQ 정보(FaqVo) 객체
	 */
	FaqVo getFaqById(Integer id);
}