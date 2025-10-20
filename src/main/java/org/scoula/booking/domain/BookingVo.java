package org.scoula.booking.domain;

import java.util.Date;

import org.scoula.booking.dto.DocInfoDto;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 예약 정보를 담는 VO(Value Object) 클래스
 */
@ApiModel(value = "예약 정보 VO", description = "DB의 booking 테이블과 매핑되는 핵심 객체")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingVo {

	/** 예약 고유 ID (ULID 형식 문자열) */
	private String bookingId;

	/** 외부 공개용 예약 코드 (날짜+지점+순번 등) */
	private String bookingCode;

	/** 예약 지점 ID */
	private int branchId;

	/** 예약자 이메일 */
	private String email;

	/** 금융 상품 코드 */
	private String finPrdtCode;

	/** 예약 날짜 (날짜 부분만 의미, 시간은 별도 필드로 관리) */
	private Date date;

	/** 예약 시간 (HH:mm 형식 문자열) */
	private String time;

	/** 예약 관련 서류 정보 DTO */
	private DocInfoDto docInfo;
}
