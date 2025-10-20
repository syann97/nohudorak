package org.scoula.booking.dto;

import org.scoula.booking.domain.BookingVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "예약 생성 응답 DTO", description = "예약 생성 성공 시 생성된 예약 번호를 반환하는 데이터")
public class BookingCreateResponseDto {

	@ApiModelProperty(value = "외부 공개용 예약 번호", example = "250810-GANGNAM-001")
	private String bookingCode;

	@ApiModelProperty(value = "예약에 필요한 서류 정보")
	private DocInfoDto docInfo;

	public static BookingCreateResponseDto of(BookingVo booking) {
		return BookingCreateResponseDto.builder()
			.bookingCode(booking.getBookingCode())
			.docInfo(booking.getDocInfo())
			.build();
	}
}