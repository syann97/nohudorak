package org.scoula.booking.dto;

import java.text.SimpleDateFormat;

import org.scoula.booking.domain.BookingVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@ApiModel(value = "예약 확인 상세 DTO", description = "예약 확인 시 사용되는 상세 정보 객체")
public class BookingCheckDetailDto {

	@ApiModelProperty(value = "예약 ID", example = "bkg-1234abcd")
	private String bookingId;

	@ApiModelProperty(value = "예약 날짜 (yyyy-MM-dd)", example = "2025-07-28")
	private String date;

	@ApiModelProperty(value = "예약 시간 (HH:mm)", example = "14:30")
	private String time;

	@ApiModelProperty(value = "지점 ID", example = "101")
	private int branchId;

	public static BookingCheckDetailDto from(BookingVo booking) {
		String formattedDate = null;
		if (booking.getDate() != null) {
			formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(booking.getDate());
		}

		String formattedTime = null;
		if (booking.getTime() != null && booking.getTime().length() >= 5) {
			formattedTime = booking.getTime().substring(0, 5);
		}

		return BookingCheckDetailDto.builder()
			.bookingId(booking.getBookingId())
			.date(formattedDate)
			.time(formattedTime)
			.branchId(booking.getBranchId())
			.build();
	}
}
