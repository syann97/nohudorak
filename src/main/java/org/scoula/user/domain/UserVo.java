package org.scoula.user.domain;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 데이터베이스 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVo {
	/** 사용자 이메일 (PK) */
	private String email;
	/** 사용자 이름 */
	private String userName;
	/** 사용자 전화번호 */
	private String userPhone;
	/** 생년월일 */
	private Date birth;
	/** 마이데이터 연동 ID */
	private String connectedId;
	/** 선호 지점 ID (FK) */
	private Integer branchId;
	/** 총 자산 */
	private Long asset;
	/** 프로필 이미지 파일명 1 */
	private String filename1;
	/** 프로필 이미지 파일명 2 */
	private String filename2;
	/** 투자 성향 점수 */
	private Double tendency;
	/** 자산 비율 */
	private Double assetProportion;
	/** 사용자의 포인트 */
	private int point;
}
