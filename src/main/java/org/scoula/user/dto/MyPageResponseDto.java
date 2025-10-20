package org.scoula.user.dto;

import java.util.List;

import org.scoula.booking.dto.BookingDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(value = "마이페이지 응답 DTO", description = "마이페이지에 필요한 모든 데이터를 담는 통합 DTO")
public class MyPageResponseDto {
	@ApiModelProperty(value = "사용자 정보 및 자산 현황 그래프 데이터")
	private UserGraphDto userInfo;

	@ApiModelProperty(value = "다가오는 예약 내역 요약 리스트")
	private List<BookingDto> bookingInfo;

	@ApiModelProperty(value = "내 자산의 상위 백분위 (단위: %)", example = "15.5")
	private Double assetPercentile;
}
