package org.scoula.exception;

/**
 * 상품(맞춤 또는 일반)이 null일 시 발생하는 예외
 */
public class ProductNotFoundException extends RuntimeException {
	public ProductNotFoundException(String finPrdtCd) {
		super("상품을 찾을 수 없습니다: " + finPrdtCd);
	}
}
