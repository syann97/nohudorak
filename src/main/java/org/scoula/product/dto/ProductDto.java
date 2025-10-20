package org.scoula.product.dto;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 최상위 부모 DTO 클래스(상품 목록 조회 시 사용되는 공통 필드)
 * @param <T> 옵션 DTO 타입
 */
@ApiModel(value = "ProductDto", description = "상품 목록 조회 시 사용되는 최상위 DTO 클래스")
@SuperBuilder
@ToString
@Getter
public class ProductDto<T> {

	@ApiModelProperty(value = "상품코드")
	private String finPrdtCd;

	@ApiModelProperty(value = "상품명")
	private String finPrdtNm;

	@ApiModelProperty(value = "상품특성")
	private String prdtFeature;

	@ApiModelProperty(value = "옵션 리스트")
	private List<T> optionList;
}
