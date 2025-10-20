package org.scoula.asset.dto;

import org.scoula.asset.domain.CategoryVo;

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
@ApiModel(value = "자산 카테고리 DTO", description = "자산 카테고리 정보 객체")
public class CategoryDto {

	@ApiModelProperty(value = "자산 카테고리 코드", example = "01")
	private String assetCategoryCode;

	@ApiModelProperty(value = "카테고리명", example = "예적금")
	private String name;

	public static CategoryDto from(CategoryVo category) {
		return CategoryDto.builder()
			.assetCategoryCode(category.getAssetCategoryCode())
			.name(category.getName())
			.build();
	}

	public CategoryVo toVo() {
		return CategoryVo.builder()
			.assetCategoryCode(assetCategoryCode)
			.name(name)
			.build();
	}
}