package org.scoula.recommend.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.recommend.domain.CustomRecommendVo;

@Mapper
public interface CustomRecommendMapper {

	/**
	 * 특정 사용자의 맞춤 추천 상품 목록을 조회합니다.
	 * @param email 사용자 이메일
	 * @return 맞춤 추천 상품(CustomRecommendVo) 리스트
	 */
	List<CustomRecommendVo> getCustomRecommendsByEmail(String email);

	/**
	 * 새로운 맞춤 추천 상품 정보를 삽입합니다.
	 * @param customRecommend 삽입할 추천 상품 정보
	 */
	void insertCustomRecommend(CustomRecommendVo customRecommend);

	/**
	 * 기존 맞춤 추천 상품 정보를 수정합니다.
	 * @param customRecommend 수정할 추천 상품 정보
	 * @return 수정된 행의 수
	 */
	int updateCustomRecommend(CustomRecommendVo customRecommend);

	/**
	 * 특정 사용자의 특정 맞춤 추천 상품을 삭제합니다.
	 * @param email 사용자 이메일
	 * @param code 삭제할 금융 상품 코드
	 * @return 삭제된 행의 수
	 */
	int deleteCustomRecommend(@Param("email") String email, @Param("code") String code);

	/**
	 * 특정 사용자의 모든 맞춤 추천 상품을 삭제합니다.
	 * @param email 사용자 이메일
	 * @return 삭제된 행의 수
	 */
	int deleteAllProductsByEmail(String email);
}