package org.scoula.View.home.dto;

import java.util.List;

import org.scoula.booking.dto.BookingDetailResponseDto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(value = "홈 페이지 응답 DTO", description = "홈 화면에 필요한 모든 데이터를 담는 통합 DTO")
public class HomeResponseDto {
	@ApiModelProperty(value = "로그인한 사용자의 요약 정보 (비로그인 시 null)")
	private UserSummary userSummary;

	@ApiModelProperty(value = "추천 상품 Top 3 목록")
	private List<RecommendationDto> recommandTop3;

	@ApiModelProperty(value = "가장 가까운 예약 내역 (없을 경우 null)")
	private BookingDetailResponseDto nearestBooking;
}