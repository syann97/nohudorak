package org.scoula.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.auth.dto.RefreshTokenDto;

/**
 * 리프레시 토큰 관련 데이터베이스 작업을 위한 MyBatis 매퍼 인터페이스
 */
@Mapper
public interface RefreshTokenMapper {
	/**
	 * 리프레시 토큰을 저장하거나 이미 존재하면 갱신합니다.
	 * @param refreshToken 저장 또는 갱신할 토큰 정보 DTO
	 */
	void saveRefreshToken(RefreshTokenDto refreshToken);

	/**
	 * 사용자 이메일을 기준으로 리프레시 토큰을 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return 조회된 리프레시 토큰 정보 DTO, 없으면 null
	 */
	RefreshTokenDto findTokenByUserEmail(@Param("email") String email);

	/**
	 * 특정 사용자의 리프레시 토큰을 삭제합니다. (로그아웃 시 사용)
	 * @param email 삭제할 토큰의 소유자 이메일
	 * @return 삭제된 행의 수
	 */
	int deleteByEmail(@Param("email") String email);
}
