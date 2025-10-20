package org.scoula.asset.service;

import java.util.List;

import org.scoula.asset.dto.CategoryDto;

/**
 * 자산 카테고리 관련 서비스 인터페이스.
 */
public interface CategoryService {

	/**
	 * 모든 자산 카테고리를 조회합니다.
	 *
	 * @return 카테고리 DTO 리스트
	 */
	List<CategoryDto> getAllCategories();

	/**
	 * 카테고리 코드를 기준으로 단일 카테고리를 조회합니다.
	 *
	 * @param assetCategoryCode 카테고리 코드
	 * @return 카테고리 DTO
	 */
	CategoryDto getCategoryByCode(String assetCategoryCode);

	/**
	 * 새로운 카테고리를 추가합니다.
	 *
	 * @param categoryDto 추가할 카테고리 DTO
	 */
	void addCategory(CategoryDto categoryDto);

	/**
	 * 기존 카테고리를 수정합니다.
	 *
	 * @param categoryDto 수정할 카테고리 DTO
	 */
	void updateCategory(CategoryDto categoryDto);

	/**
	 * 카테고리를 삭제합니다.
	 *
	 * @param assetCategoryCode 삭제할 카테고리 코드
	 */
	void deleteCategory(String assetCategoryCode);
}
