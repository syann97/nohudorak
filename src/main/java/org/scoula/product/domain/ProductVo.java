package org.scoula.product.domain;

import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 금융상품들의 부모 클래스입니다.
 * @param <T> 옵션 DTO 타입
 */
@ApiModel(value = "ProductVo", description = "금융상품 공통 정보")
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = {"finPrdtCd"})
public class ProductVo<T> {

	@ApiModelProperty(value = "금융회사 코드", example = "001")
	private String finCoNo;

	@ApiModelProperty(value = "금융회사명", example = "한국은행")
	private String korCoNm;

	@ApiModelProperty(value = "상품 카테고리", example = "예금")
	private String finPrdtCategory;

	@ApiModelProperty(value = "상품 코드", example = "D001")
	private String finPrdtCd;

	@ApiModelProperty(value = "상품명", example = "정기예금 1년")
	private String finPrdtNm;

	@ApiModelProperty(value = "상품 특성", example = "고정금리")
	private String prdtFeature;

	@ApiModelProperty(value = "상품 설명", example = "1년 만기 정기예금 상품")
	private String description;

	@ApiModelProperty(value = "가입 경로", example = "온라인")
	private String joinWay;

	@ApiModelProperty(value = "추천 사유", example = "안정적 투자")
	private String recReason;

	@ApiModelProperty(value = "투자성향", example = "0.5")
	private Double tendency;

	@ApiModelProperty(value = "자산 구성 비율", example = "0.3")
	private Double assetProportion;

	@ApiModelProperty(value = "옵션 리스트")
	private List<T> optionList;
}
