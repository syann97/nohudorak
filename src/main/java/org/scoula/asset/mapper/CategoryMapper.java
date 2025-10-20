package org.scoula.asset.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.scoula.asset.domain.CategoryVo;

/**
 * 자산 카테고리(Category) 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
@Mapper
public interface CategoryMapper {
	/**
	 * 모든 카테고리 목록을 조회합니다.
	 * @return 카테고리 리스트
	 */
	List<CategoryVo> getAllCategories();

	/**
	 * 카테고리 코드로 특정 카테고리를 조회합니다.
	 * @param code 조회할 카테고리의 코드
	 * @return 카테고리 정보 객체
	 */
	CategoryVo getCategoryByCode(String code);

	/**
	 * 새로운 카테고리를 데이터베이스에 추가합니다.
	 * @param category 추가할 카테고리 정보
	 */
	void insertCategory(CategoryVo category);

	/**
	 * 기존 카테고리 정보를 수정합니다.
	 * @param category 수정할 카테고리 정보
	 * @return 수정된 행의 수
	 */
	int updateCategory(CategoryVo category);

	/**
	 * 특정 카테고리를 삭제합니다.
	 * @param code 삭제할 카테고리의 코드
	 * @return 삭제된 행의 수
	 */
	int deleteCategory(String code);
}