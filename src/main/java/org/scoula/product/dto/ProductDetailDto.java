package org.scoula.product.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 중간 부모 클래스(상품 상세조회 시 사용되는 DTO)
 * @param <T> 옵션 DTO 타입
 */
@ApiModel(value = "ProductDetailDto", description = "상품 상세 조회용 DTO의 공통 상위 클래스")
@SuperBuilder
@ToString
@Getter
public class ProductDetailDto<T> extends ProductDto<T> {

	@ApiModelProperty(value = "금융회사명")
	private String korCoNm;

	@ApiModelProperty(value = "상품 카테고리")
	private String finPrdtCategory;

	@ApiModelProperty(value = "상품설명")
	private String description;

	@ApiModelProperty(value = "가입경로")
	private String joinWay;

	@ApiModelProperty(value = "추천 사유")
	private String recReason;
}
