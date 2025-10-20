package org.scoula.gift.domain;

import java.util.Date;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(value = "수증자 VO", description = "DB의 recipient 테이블과 매핑되는 핵심 객체")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipientVo {
	@ApiModelProperty(value = "수증자 고유 ID", example = "1")
	private Integer recipientId;

	@ApiModelProperty(value = "사용자 이메일 (FK)", example = "user@example.com")
	private String email;

	@ApiModelProperty(value = "증여자와의 관계", example = "자녀")
	private String relationship;

	@ApiModelProperty(value = "수증자 이름", example = "홍길동")
	private String recipientName;

	@ApiModelProperty(value = "수증자 생년월일", example = "1995-05-10")
	private Date birthDate;

	@ApiModelProperty(value = "결혼 여부", example = "true")
	private Boolean isMarried;

	@ApiModelProperty(value = "10년 이내 증여 여부", example = "false")
	private Boolean hasPriorGift;

	@ApiModelProperty(value = "10년 이내 증여액 (없는 경우 0)", example = "0")
	private Long priorGiftAmount;

	@ApiModelProperty(value = "증여세 납부 주체", example = "증여자")
	private String giftTaxPayer;
}