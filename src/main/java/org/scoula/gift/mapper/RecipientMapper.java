package org.scoula.gift.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.scoula.gift.domain.RecipientVo;

/**
 * RecipientMapper: 'recipient' 테이블에 접근하는 MyBatis 매퍼 인터페이스.
 * SQL 쿼리는 resources/mapper/RecipientMapper.xml 파일에 정의됩니다.
 */
public interface RecipientMapper {

	/**
	 * 수증자 정보를 데이터베이스에 삽입합니다.
	 * @param recipientVo 삽입할 수증자 정보가 담긴 VO
	 */
	void insertRecipient(RecipientVo recipientVo);

	/**
	 * 특정 사용자의 모든 수증자 목록을 조회합니다.
	 * @param email 조회할 사용자의 이메일
	 * @return 수증자 정보 VO 리스트
	 */
	List<RecipientVo> findByEmail(String email);

	/**
	 * 수증자 ID(PK)로 특정 수증자 정보를 조회합니다.
	 * @param recipientId 조회할 수증자 ID
	 * @return 조회된 수증자 정보 VO
	 */
	RecipientVo findById(Integer recipientId);

	/**
	 * 수증자 ID와 사용자 이메일로 특정 수증자 정보를 조회합니다. (본인 소유 데이터인지 권한 확인용)
	 * @param recipientId 조회할 수증자 ID
	 * @param email 조회할 사용자의 이메일
	 * @return 조회된 수증자 정보 VO
	 */
	RecipientVo findByIdAndEmail(@Param("recipientId") Integer recipientId, @Param("email") String email);

	/**
	 * 수증자 정보를 수정합니다.
	 * @param recipientVo 수정할 내용이 담긴 VO
	 * @return 영향을 받은 행의 수
	 */
	int updateRecipient(RecipientVo recipientVo);

	/**
	 * 수증자 정보를 삭제합니다.
	 * @param recipientId 삭제할 수증자 ID
	 * @return 영향을 받은 행의 수
	 */
	int deleteById(Integer recipientId);

	/**
	 * 특정 이메일에 해당하는 모든 수증자 정보를 삭제합니다. (회원 탈퇴 시 사용)
	 * @param email 삭제할 사용자의 이메일
	 * @return 삭제된 행의 수
	 */
	int deleteByEmail(String email);
}
