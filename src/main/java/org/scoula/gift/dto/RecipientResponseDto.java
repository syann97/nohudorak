package org.scoula.gift.dto;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.scoula.gift.domain.RecipientVo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "수증자 정보 응답 DTO", description = "수증자 정보 조회 시 클라이언트에 반환하는 데이터")
@Data
@Builder
public class RecipientResponseDto {

	@ApiModelProperty(value = "수증자 ID (PK)", example = "1")
	private Integer recipientId;

	@ApiModelProperty(value = "관계", example = "자녀")
	private String relationship;

	@ApiModelProperty(value = "수증자 이름", example = "홍길동")
	private String recipientName;

	@ApiModelProperty(value = "생년월일", example = "2000-01-01")
	private String birthDate; // Date 타입을 String으로 변경

	@ApiModelProperty(value = "결혼 여부", example = "false")
	private Boolean isMarried;

	@ApiModelProperty(value = "사전 증여 여부", example = "true")
	private Boolean hasPriorGift;

	@ApiModelProperty(value = "사전 증여 가액", example = "10000000")
	private Long priorGiftAmount;

	@ApiModelProperty(value = "증여세 납부자", example = "수증자")
	private String giftTaxPayer;

	/**
	 * RecipientVo 객체를 RecipientResponseDto로 변환합니다.
	 * 이 과정에서 Date 객체를 'YYYY-MM-DD' 형식의 문자열로 포맷팅합니다.
	 *
	 * @param vo 변환할 RecipientVo 객체
	 * @return 변환된 RecipientResponseDto 객체
	 */
	public static RecipientResponseDto from(RecipientVo vo) {
		String formattedDate = "";
		if (vo.getBirthDate() != null) {
			// 날짜 포맷을 지정합니다.
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			formattedDate = sdf.format(vo.getBirthDate());
		}

		return RecipientResponseDto.builder()
			.recipientId(vo.getRecipientId())
			.relationship(vo.getRelationship())
			.recipientName(vo.getRecipientName())
			.birthDate(formattedDate) // 포맷팅된 문자열을 설정
			.isMarried(vo.getIsMarried())
			.hasPriorGift(vo.getHasPriorGift())
			.priorGiftAmount(vo.getPriorGiftAmount())
			.giftTaxPayer(vo.getGiftTaxPayer())
			.build();
	}
}
