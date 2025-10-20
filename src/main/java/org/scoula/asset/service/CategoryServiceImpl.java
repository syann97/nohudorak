package org.scoula.asset.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.scoula.asset.dto.CategoryDto;
import org.scoula.asset.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

/**
 * 카테고리 관련 비즈니스 로직을 처리하는 서비스 구현 클래스
 */
@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryMapper categoryMapper;

	/**
	 * 모든 카테고리 목록을 조회합니다.
	 * @return CategoryDto 리스트
	 */
	@Override
	public List<CategoryDto> getAllCategories() {
		// DB에서 조회한 CategoryVo 리스트를 CategoryDto 리스트로 변환하여 반환합니다.
		return categoryMapper.getAllCategories().stream()
			.map(CategoryDto::from)
			.collect(Collectors.toList());
	}

	/**
	 * 코드로 특정 카테고리를 조회합니다.
	 * @param assetCategoryCode 조회할 카테고리 코드
	 * @return 해당 CategoryDto
	 * @throws NoSuchElementException 해당 코드를 가진 카테고리가 없을 경우 발생합니다.
	 */
	@Override
	public CategoryDto getCategoryByCode(String assetCategoryCode) {
		// Optional을 사용하여 null 체크 후, 카테고리가 없으면 예외를 던집니다.
		return Optional.ofNullable(categoryMapper.getCategoryByCode(assetCategoryCode))
			.map(CategoryDto::from)
			.orElseThrow(() -> new NoSuchElementException("Category not found with code: " + assetCategoryCode));
	}

	/**
	 * 새로운 카테고리를 추가합니다.
	 * @param categoryDto 추가할 카테고리 정보 DTO
	 */
	@Override
	public void addCategory(CategoryDto categoryDto) {
		// DTO를 VO로 변환하여 DB에 삽입합니다.
		categoryMapper.insertCategory(categoryDto.toVo());
	}

	/**
	 * 기존 카테고리 정보를 수정합니다.
	 * @param categoryDto 수정할 카테고리 정보 DTO
	 * @throws NoSuchElementException 수정할 카테고리가 존재하지 않을 경우 발생합니다.
	 */
	@Override
	public void updateCategory(CategoryDto categoryDto) {
		// update 실행 후 변경된 행의 수가 0이면, 해당 데이터가 없는 것이므로 예외를 던집니다.
		if (categoryMapper.updateCategory(categoryDto.toVo()) == 0) {
			throw new NoSuchElementException("Category not found with code: " + categoryDto.getAssetCategoryCode());
		}
	}

	/**
	 * 특정 카테고리를 삭제합니다.
	 * @param assetCategoryCode 삭제할 카테고리 코드
	 * @throws NoSuchElementException 삭제할 카테고리가 존재하지 않을 경우 발생합니다.
	 */
	@Override
	public void deleteCategory(String assetCategoryCode) {
		// delete 실행 후 변경된 행의 수가 0이면, 해당 데이터가 없는 것이므로 예외를 던집니다.
		if (categoryMapper.deleteCategory(assetCategoryCode) == 0) {
			throw new NoSuchElementException("Category not found with code: " + assetCategoryCode);
		}
	}
}