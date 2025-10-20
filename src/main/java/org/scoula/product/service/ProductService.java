package org.scoula.product.service;

import java.util.List;
import java.util.Map;

import org.scoula.product.domain.FundDailyReturnVo;
import org.scoula.product.domain.ProductVo;
import org.scoula.product.dto.ProductDto;

/**
 * 금융 상품 관련 서비스 인터페이스
 */
public interface ProductService {

	/**
	 * 모든 금융 상품을 조회하여 Map 형태로 반환합니다.
	 * 키는 상품 카테고리, 값은 해당 카테고리의 상품 목록입니다.
	 *
	 * @return 카테고리별 금융 상품 목록 Map
	 */
	Map<String, List<? extends ProductDto>> findAllProducts();

	/**
	 * 상품코드(finPrdtCd)를 기준으로 해당 금융 상품의 상세 정보를 조회합니다.
	 *
	 * @param finPrdtCd 조회할 금융 상품 코드
	 * @return 금융 상품 상세 정보 VO
	 */
	ProductVo getProductDetail(String finPrdtCd);

	/**
	 * 상품코드(finPrdtCd)에 해당하는 금융 상품명을 조회합니다.
	 *
	 * @param finPrdtCd 조회할 금융 상품 코드
	 * @return 금융 상품명
	 */
	String getProductNameByCode(String finPrdtCd);

	/**
	 * 특정 펀드 코드(finPrdtCd)에 대한 일별 수익률 정보를 조회합니다.
	 *
	 * @param finPrdtCd 조회할 펀드 코드
	 * @return 해당 펀드의 일별 수익률 목록
	 */
	List<FundDailyReturnVo> getFundDailyReturnByCode(String finPrdtCd);
}
