package org.scoula.branch.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 지점 정보를 담는 도메인 객체 (Value Object)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchVo {
	/** 지점명 */
	private String branchName;

	/** 지점 전화번호 */
	private String branchPhone;

	/** 지번 주소 */
	private String addressName;

	/** 도로명 주소 */
	private String roadAddressName;

	/** 경도(Longitude) 좌표 */
	private String x;

	/** 위도(Latitude) 좌표 */
	private String y;

	/** 특정 위치로부터의 거리 */
	private String distance;
}