package org.scoula.retirement.dto;

import java.util.List;
import java.util.Map;

import org.scoula.news.dto.NewsDto;
import org.scoula.product.dto.ProductDto;
import org.scoula.recommend.dto.CustomRecommendDto;
import org.scoula.user.dto.UserGraphDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "노후 메인 페이지 응답 DTO", description = "노후 메인 페이지의 모든 데이터를 담는 통합 DTO")
public class RetirementMainResponseDto {
	@ApiModelProperty(value = "사용자 정보 및 자산 현황 그래프 데이터")
	private UserGraphDto userInfo;

	@ApiModelProperty(value = "맞춤 추천 상품 목록")
	private List<CustomRecommendDto> customRecommendPrdt;

	@ApiModelProperty(value = "전체 상품 목록 (예금, 적금, 주택담보대출, 펀드, 금, 신탁)")
	private Map<String, List<? extends ProductDto>> allProducts;

	@ApiModelProperty(value = "뉴스 목록")
	private List<NewsDto> news;
}
