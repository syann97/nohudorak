package org.scoula.booking.dto;


import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankBookingRequestDto {

	private String email;       // 사용자 이메일
	private Integer branchId;   // 지점 ID
	private String finPrdtCode; // 금융상품 코드
	@JsonFormat(pattern = "yyyy-MM-dd")
	private Date date;          // 예약 날짜
	private String time;        // 예약 시간
	private DocInfoDto docInfo; // 서류 정보
}
