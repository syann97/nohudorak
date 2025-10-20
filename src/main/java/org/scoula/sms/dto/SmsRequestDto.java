package org.scoula.sms.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "SMS 발송 요청 DTO", description = "SMS 발송에 필요한 정보를 담는 객체")
public class SmsRequestDto {
	// 수신자 전화번호
	// 현재는 프론트에서 받아오는데, 추후 백엔드 로그인 정보 참고로 수정?
	@ApiModelProperty(value = "수신자 전화번호", required = true, example = "01012345678")
	private String phoneNumber;

	// 예약 정보 관련 필드
	@ApiModelProperty(value = "예약 상품명", example = "신한 주거래 우대통장")
	private String productName;

	@ApiModelProperty(value = "예약 지점명", example = "KB국민은행 강남역종합금융센터")
	private String branchName;

	@ApiModelProperty(value = "예약 날짜", example = "2025-08-15")
	private String reservationDate;

	@ApiModelProperty(value = "예약 시간", example = "14:30")
	private String reservationTime;

	// 아직 못받은 사용자 이름
	@ApiModelProperty(value = "사용자 이름", example = "홍길동")
	private String userName;
}
