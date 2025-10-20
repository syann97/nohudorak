package org.scoula.news.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.news.domain.NewsVo;

/**
 * 뉴스 데이터베이스 접근을 위한 MyBatis Mapper 인터페이스
 */
@Mapper
public interface NewsMapper {

	/**
	 * 특정 카테고리의 뉴스 조회
	 * @param category 뉴스 카테고리 번호
	 * @return 해당 카테고리의 뉴스 객체
	 */
	NewsVo findByCategory(Integer category);

	/**
	 * 뉴스 정보가 존재하면 업데이트, 존재하지 않으면 삽입
	 * @param news 뉴스 객체
	 */
	void upsertNews(NewsVo news);

	/**
	 * 새로운 뉴스 레코드 삽입
	 * @param news 뉴스 객체
	 */
	void insertNews(NewsVo news);

	/**
	 * 기존 뉴스 레코드 업데이트
	 * @param news 뉴스 객체
	 */
	void updateNews(NewsVo news);

	/**
	 * 모든 뉴스 조회
	 * @return 뉴스 리스트
	 */
	List<NewsVo> findAll();
}
