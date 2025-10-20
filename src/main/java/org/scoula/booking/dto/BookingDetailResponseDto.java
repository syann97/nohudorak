package org.scoula.booking.dto;

import java.text.SimpleDateFormat;

import org.scoula.booking.domain.BookingVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "예약 상세 응답 DTO", description = "예약 상세 정보 조회 시 클라이언트에 반환하는 데이터")
public class BookingDetailResponseDto {

	@ApiModelProperty(value = "외부 공개용 예약 번호", example = "250810-GANGNAM-001")
	private String bookingCode;

	@ApiModelProperty(value = "지점명", example = "강남지점")
	private String branchName;

	@ApiModelProperty(value = "금융 상품명", example = "정기예금 1년제")
	private String prodName;

	@ApiModelProperty(value = "예약 날짜 (yyyy-MM-dd)", example = "2025-07-28")
	private String date;

	@ApiModelProperty(value = "예약 시간 (HH:mm)", example = "14:30")
	private String time;

	@ApiModelProperty(value = "필요 서류 정보")
	private DocInfoDto docInfo;

	public static BookingDetailResponseDto of(BookingVo booking, String prodName, String branchName) {
		String formattedDate = null;
		if (booking.getDate() != null) {
			formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(booking.getDate());
		}

		return BookingDetailResponseDto.builder()
			.bookingCode(booking.getBookingCode())
			.branchName(branchName)
			.prodName(prodName)
			.date(formattedDate)
			.time(booking.getTime())
			.docInfo(booking.getDocInfo())
			.build();
	}
}
