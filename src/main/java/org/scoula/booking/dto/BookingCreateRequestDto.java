package org.scoula.booking.dto;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.scoula.booking.domain.BookingVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "예약 생성 요청 DTO", description = "예약 생성을 위해 클라이언트에서 받는 데이터")
public class BookingCreateRequestDto {

	@ApiModelProperty(value = "지점 ID", example = "101", required = true)
	private int branchId;

	@ApiModelProperty(value = "금융상품 코드", example = "FIN123456", required = true)
	private String finPrdtCode;

	@ApiModelProperty(value = "예약 날짜 (yyyy-MM-dd)", example = "2025-07-28", required = true)
	private String date;

	@ApiModelProperty(value = "예약 시간 (HH:mm)", example = "14:30", required = true)
	private String time;

	/**
	 * DTO를 Vo로 변환합니다.
	 * email, docInfo, bookingUlid 등은 서비스 계층에서 별도로 설정합니다.
	 * @return BookingVo 객체
	 */
	public BookingVo toVo() {
		Date parsedDate;
		try {
			parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(this.date);
		} catch (ParseException e) {
			throw new RuntimeException("Invalid date format. Please use yyyy-MM-dd.", e);
		}

		return BookingVo.builder()
			.branchId(this.branchId)
			.finPrdtCode(this.finPrdtCode)
			.date(parsedDate)
			.time(this.time)
			.build();
	}
}
