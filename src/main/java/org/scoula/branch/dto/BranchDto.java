package org.scoula.branch.dto;

import org.scoula.branch.domain.BranchVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "지점 정보 DTO", description = "API 통신에 사용되는 지점 정보 객체")
public class BranchDto {
	@ApiModelProperty(value = "지점명", example = "KB국민은행 가산디지털종합금융센터")
	private String branchName;

	@ApiModelProperty(value = "지점 전화번호", example = "02-855-9381")
	private String branchPhone;

	@ApiModelProperty(value = "지번 주소", example = "서울 금천구 가산동 459-22")
	private String addressName;

	@ApiModelProperty(value = "도로명 주소", example = "서울 금천구 가산디지털1로 137")
	private String roadAddressName;

	@ApiModelProperty(value = "경도(Longitude) 좌표", example = "126.884523178332")
	private String x;

	@ApiModelProperty(value = "위도(Latitude) 좌표", example = "37.4811933834127")
	private String y;

	@ApiModelProperty(value = "특정 위치로부터의 거리", example = "0.5km")
	private String distance;

	/**
	 * BranchVo 객체를 BranchDto로 변환하는 정적 메소드
	 * @param branch BranchVo 객체
	 * @return 변환된 BranchDto 객체
	 */
	public static BranchDto of(BranchVo branch) {
		return BranchDto.builder()
			.branchName(branch.getBranchName())
			.branchPhone(branch.getBranchPhone())
			.addressName(branch.getAddressName())
			.roadAddressName(branch.getRoadAddressName())
			.x(branch.getX())
			.y(branch.getY())
			.distance(branch.getDistance())
			.build();
	}

	/**
	 * BranchDto 객체를 BranchVo로 변환하는 메소드
	 * @return 변환된 BranchVo 객체
	 */
	public BranchVo toVo() {
		return BranchVo.builder()
			.branchName(branchName)
			.branchPhone(branchPhone)
			.addressName(addressName)
			.roadAddressName(roadAddressName)
			.x(x)
			.y(y)
			.distance(distance)
			.build();
	}
}